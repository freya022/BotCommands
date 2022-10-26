package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.api.ExceptionHandler;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.RunnableEx;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class AutocompletionListener implements EventListener {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;

	private int autocompletionThreadNumber = 0;
	private final ExecutorService autocompletionService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in an autocompletion thread '" + t.getName() + "':", e));
		thread.setName("Autocompletion thread #" + autocompletionThreadNumber++);

		return thread;
	});

	public AutocompletionListener(BContextImpl context) {
		this.context = context;
	}

	@SubscribeEvent
	@Override
	public void onEvent(@NotNull GenericEvent genericEvent) {
		if (genericEvent instanceof CommandAutoCompleteInteractionEvent event) {
			final Consumer<Throwable> throwableConsumer = getThrowableConsumer(event);

			runAutocompletion(() -> {
				final SlashCommandInfo slashCommand = context.getApplicationCommandsContext().findLiveSlashCommand(event.getGuild(), CommandPath.of(event.getCommandPath()));

				if (slashCommand == null) {
					LOGGER.warn("Slash command not found during autocompletion for '{}'", event.getCommandPath());

					return;
				}

				final String autocompletionHandler = slashCommand.getAutocompletionHandlerName(event);
				if (autocompletionHandler == null) {
					LOGGER.warn("Found no autocompletion handler name for option '{}' in command '{}'", event.getFocusedOption().getName(), slashCommand.getPath());

					return;
				}

				final AutocompletionHandlerInfo handler = context.getAutocompletionHandler(autocompletionHandler);
				if (handler == null) {
					LOGGER.warn("Found no autocompletion handler for '{}'", autocompletionHandler);

					return;
				}

				//I really don't like the internal chaining of result consumers...
				handler.retrieveChoices(slashCommand, event, throwableConsumer, choices -> {
					event.replyChoices(choices).queue();
				});
			}, throwableConsumer);
		}
	}

	private void runAutocompletion(RunnableEx code, Consumer<Throwable> throwableConsumer) {
		autocompletionService.execute(() -> {
			try {
				code.run();
			} catch (Throwable e) {
				throwableConsumer.accept(e);
			}
		});
	}

	private Consumer<Throwable> getThrowableConsumer(CommandAutoCompleteInteractionEvent event) {
		return e -> {
			final ExceptionHandler handler = context.getUncaughtExceptionHandler();
			if (handler != null) {
				handler.onException(context, event, e);

				return;
			}

			Throwable baseEx = Utils.getException(e);

			Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while autocompleting a command option '" + reconstructCommand(event) + "'", baseEx);
			if (!event.isAcknowledged()) {
				event.replyChoices().queue();
			}

			context.dispatchException("Exception while autocompleting '" + reconstructCommand(event) + "'", baseEx);
		};
	}

	private String reconstructCommand(CommandAutoCompleteInteractionEvent event) {
		return event.getCommandString();
	}
}
