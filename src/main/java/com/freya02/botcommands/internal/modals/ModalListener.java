package com.freya02.botcommands.internal.modals;

import com.freya02.botcommands.api.ExceptionHandler;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.RunnableEx;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class ModalListener implements EventListener {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;

	private int commandThreadNumber = 0;
	private final ExecutorService commandService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in a modal handling thread '" + t.getName() + "':", e));
		thread.setName("Modal handling thread #" + commandThreadNumber++);

		return thread;
	});

	public ModalListener(BContextImpl context) {
		this.context = context;
	}

	@SubscribeEvent
	@Override
	public void onEvent(@NotNull GenericEvent e) {
		if (e instanceof ModalInteractionEvent event) {
			final Consumer<Throwable> throwableConsumer = getThrowableConsumer(event);

			runCommand(() -> {
				final ModalData modalData = context.getModalMaps().consumeModal(event.getModalId());

				if (modalData == null) { //Probably the modal expired
					event.reply(context.getDefaultMessages(event.getUserLocale()).getModalExpiredErrorMsg())
							.setEphemeral(true)
							.queue();

					return;
				}

				final ModalHandlerInfo modalHandler = context.getApplicationCommandsContext().getModalHandler(modalData.getHandlerName());

				if (modalHandler == null) {
					LOGGER.warn("Got no modal handler for handler name: '{}'", modalData.getHandlerName());

					//TODO localize
					event.reply("Modal handler not found")
							.setEphemeral(true)
							.queue();

					return;
				}

				modalHandler.execute(context, modalData, event, throwableConsumer);
			}, throwableConsumer);
		}
	}

	private Consumer<Throwable> getThrowableConsumer(ModalInteractionEvent event) {
		return e -> {
			final ExceptionHandler handler = context.getUncaughtExceptionHandler();
			if (handler != null) {
				handler.onException(context, event, e);

				return;
			}

			Throwable baseEx = Utils.getException(e);

			Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing a modal handler", baseEx);
			if (event.isAcknowledged()) {
				event.getHook().sendMessage(context.getDefaultMessages(event.getGuild()).getApplicationCommandErrorMsg()).setEphemeral(true).queue();
			} else {
				event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandErrorMsg()).setEphemeral(true).queue();
			}

			context.dispatchException("Exception in modal handler", baseEx);
		};
	}

	private void runCommand(RunnableEx code, Consumer<Throwable> throwableConsumer) {
		commandService.execute(() -> {
			try {
				code.run();
			} catch (Throwable e) {
				throwableConsumer.accept(e);
			}
		});
	}
}
