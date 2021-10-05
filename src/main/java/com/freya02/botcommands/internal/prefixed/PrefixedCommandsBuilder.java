package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class PrefixedCommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;

	public PrefixedCommandsBuilder(@NotNull BContextImpl context) {
		this.context = context;
	}

	public void processPrefixedCommand(TextCommand command, Method method) {
		try {
			if (!method.canAccess(command)) //TODO move canAccess to CommandsBuilderImpl#processClass
				throw new IllegalStateException("Application command " + Utils.formatMethodShort(method) + " is not public");

			if (!Utils.hasFirstParameter(method, BaseCommandEvent.class)) //Handles CommandEvent (and subtypes) too
				throw new IllegalArgumentException("Prefixed command at " + Utils.formatMethodShort(method) + " must have a BaseCommandEvent or a CommandEvent as first parameter");

			final TextCommandInfo info = new TextCommandInfo(command, method);

			context.addTextCommand(info);

			if (info.isRegexCommand()) {
				LOGGER.debug("Added prefixed command path {} for method {} with pattern {}", info.getPath(), Utils.formatMethodShort(method), info.getCompletePattern());
			} else {
				LOGGER.debug("Added prefixed command path {} for method {}", info.getPath(), Utils.formatMethodShort(method));
			}
		} catch (Exception e) {
			throw new RuntimeException("An exception occurred while processing prefixed command at " + Utils.formatMethodShort(method), e);
		}
	}

	public void postProcess() {
		for (TextCommandCandidates command : context.getCommands()) {
			for (TextCommandInfo info : command) {
				for (TextCommandInfo commandInfo : command) {
					if (info == commandInfo) continue;

					final Method commandMethod1 = info.getCommandMethod();
					final Method commandMethod2 = commandInfo.getCommandMethod();

					final List<? extends TextCommandParameter> parameters1 = info.getOptionParameters();
					final List<? extends TextCommandParameter> parameters2 = commandInfo.getOptionParameters();

					if (parameters1.stream().map(CommandParameter::getParameter).collect(Collectors.toList()).equals(parameters2.stream().map(CommandParameter::getParameter).collect(Collectors.toList()))) {
						throw new IllegalStateException("Method " + Utils.formatMethodShort(commandMethod1) + " has the same parameters as " + Utils.formatMethodShort(commandMethod2));
					}
				}
			}
		}
	}
}
