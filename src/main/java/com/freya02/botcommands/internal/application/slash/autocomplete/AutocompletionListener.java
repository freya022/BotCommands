package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.api.ExceptionHandler;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.RunnableEx;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.CommandAutoCompleteEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class AutocompletionListener implements EventListener {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	private final Map<String, AutocompletionHandlerInfo> autocompletionHandlersMap;

	private int autocompletionThreadNumber = 0;
	private final ExecutorService autocompletionService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in an autocompletion thread '" + t.getName() + "':", e));
		thread.setName("Autocompletion thread #" + autocompletionThreadNumber++);

		return thread;
	});

	public AutocompletionListener(BContextImpl context, Map<String, AutocompletionHandlerInfo> autocompletionHandlersMap) {
		this.context = context;
		this.autocompletionHandlersMap = autocompletionHandlersMap;
	}

	@Override
	public void onEvent(@NotNull GenericEvent genericEvent) {
		if (genericEvent instanceof CommandAutoCompleteEvent event) {
			runAutocompletion(() -> {
				final SlashCommandInfo slashCommand = context.findSlashCommand(CommandPath.of(event.getCommandPath()));

				if (slashCommand == null) {
					event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandNotFoundMsg()).queue();
					return;
				}

				final String autocompletionHandler = slashCommand.getAutocompletionHandlerName(event);
				if (autocompletionHandler == null) {
					LOGGER.warn("Found no autocompletion handler name for option '{}' in command '{}'", event.getFocusedOptionType().getName(), slashCommand.getPath());

					return;
				}

				final AutocompletionHandlerInfo handler = autocompletionHandlersMap.get(autocompletionHandler);
				if (handler == null) {
					LOGGER.warn("Found no autocompletion handler for '{}'", autocompletionHandler);

					return;
				}

				event.respondChoices(handler.getChoices(slashCommand, event)).queue();
			}, event);
		}
	}

	private void runAutocompletion(RunnableEx code, CommandAutoCompleteEvent event) {
		autocompletionService.execute(() -> {
			try {
				code.run();
			} catch (Throwable e) {
				final ExceptionHandler handler = context.getUncaughtExceptionHandler();
				if (handler != null) {
					handler.onException(context, event, e);

					return;
				}

				Throwable baseEx = Utils.getException(e);

				Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while autocompleting a command option '" + reconstructCommand(event) + "'", baseEx);
				if (event.isAcknowledged()) {
					event.getHook().sendMessage(context.getDefaultMessages(event.getGuild()).getApplicationCommandErrorMsg()).setEphemeral(true).queue();
				} else {
					event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandErrorMsg()).setEphemeral(true).queue();
				}

				context.dispatchException("Exception while autocompleting '" + reconstructCommand(event) + "'", baseEx);
			}
		});
	}

	private String reconstructCommand(CommandAutoCompleteEvent event) {
		StringBuilder builder = new StringBuilder("/" + event.getName());
		if (event.getSubcommandGroup() != null)
			builder.append(' ').append(event.getSubcommandGroup());
		if (event.getSubcommandName() != null)
			builder.append(' ').append(event.getSubcommandName());
		return builder.toString();
	}
}
