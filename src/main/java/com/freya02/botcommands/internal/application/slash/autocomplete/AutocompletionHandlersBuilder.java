package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandParameter;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class AutocompletionHandlersBuilder {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;

	public AutocompletionHandlersBuilder(BContextImpl context) {
		this.context = context;
	}

	public void processHandler(Object autocompleteHandler, Method method) {
		try {
			if (!ReflectionUtils.hasFirstParameter(method, CommandAutoCompleteInteractionEvent.class))
				throw new IllegalArgumentException("Autocompletion handler at " + Utils.formatMethodShort(method) + " must have a " + CommandAutoCompleteInteractionEvent.class.getSimpleName() + " event as first parameter");

			final AutocompletionHandlerInfo handler = new AutocompletionHandlerInfo(context, autocompleteHandler, method);

			context.addAutocompletionHandler(handler);

			LOGGER.debug("Adding autocompletion handler '{}' for method {}", handler.getHandlerName(), Utils.formatMethodShort(method));
		} catch (Exception e) {
			throw new RuntimeException("An exception occurred while processing an autocompletion handler at " + Utils.formatMethodShort(method), e);
		}
	}

	public void postProcess() {
		for (SlashCommandInfo info : context.getApplicationCommandInfoMap().getSlashCommands().values()) {
			MethodParameters<SlashCommandParameter> parameters = info.getParameters();
			for (int i = 0, parametersSize = parameters.size(); i < parametersSize; i++) {
				SlashCommandParameter parameter = parameters.get(i);

				if (!parameter.isOption()) continue;

				final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

				final String autocompleteHandlerName = applicationOptionData.getAutocompletionHandlerName();

				if (autocompleteHandlerName != null) {
					final AutocompletionHandlerInfo handler = context.getAutocompletionHandler(autocompleteHandlerName);

					if (handler == null) {
						throw new IllegalArgumentException("Slash command parameter #" + i + " at " + Utils.formatMethodShort(info.getMethod()) + " uses autocompletion but has no handler assigned, did you misspell the handler name ? Consider using a constant variable to share with the handler and the option");
					}

					handler.checkParameters(info);
				}
			}
		}

		context.addEventListeners(new AutocompletionListener(context));
	}
}
