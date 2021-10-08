package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.JdaTextCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.AbstractCommandInfo;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TextCommandInfo extends AbstractCommandInfo<TextCommand> {
	private static final Logger LOGGER = Logging.getLogger();
	private final List<CommandPath> aliases;

	private final String description;

	private final boolean requireOwner;
	private final boolean hidden;

	private final MethodParameters<TextCommandParameter> parameters;

	private final Pattern completePattern;
	private final int order;

	public TextCommandInfo(TextCommand command, Method commandMethod) {
		super(command,
				commandMethod.getAnnotation(JdaTextCommand.class),
				commandMethod,
				commandMethod.getAnnotation(JdaTextCommand.class).name(),
				commandMethod.getAnnotation(JdaTextCommand.class).group(),
				commandMethod.getAnnotation(JdaTextCommand.class).subcommand()
				);

		final JdaTextCommand jdaCommand = commandMethod.getAnnotation(JdaTextCommand.class);

		aliases = Arrays.stream(jdaCommand.aliases()).map(CommandPath::of).collect(Collectors.toList());
		description = jdaCommand.description();

		order = jdaCommand.order();

		final boolean isRegexMethod = !Utils.hasFirstParameter(commandMethod, CommandEvent.class);
		parameters = MethodParameters.of(commandMethod, (parameter, index) -> {
			if (parameter.isAnnotationPresent(AppOption.class))
				throw new IllegalArgumentException(String.format("Text command parameter #%d of %s#%s cannot be annotated with @AppOption", index, commandMethod.getDeclaringClass().getName(), commandMethod.getName()));

			//Fallback doesn't accept options
			if (parameter.isAnnotationPresent(TextOption.class) && !isRegexMethod)
				throw new IllegalArgumentException("Fallback text commands (CommandEvent ones) cannot have parameters annotated with @TextOption");

			return new TextCommandParameter(RegexParameterResolver.class, parameter, index);
		});

		hidden = AnnotationUtils.getEffectiveHiddenState(commandMethod);
		requireOwner = AnnotationUtils.getEffectiveRequireOwnerState(commandMethod);

		if (parameters.getOptionCount() > 0) {
			completePattern = CommandPattern.of(this);
		} else completePattern = null;
	}

	public boolean isOwnerRequired() {
		return requireOwner;
	}

	public boolean isHidden() {
		return hidden;
	}

	public List<CommandPath> getAliases() {
		return aliases;
	}

	public String getDescription() {
		return description;
	}

	public int getOrder() {
		return order;
	}

	@Nullable
	public Pattern getCompletePattern() {
		return completePattern;
	}

	@Override
	public MethodParameters<TextCommandParameter> getParameters() {
		return parameters;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends TextCommandParameter> getOptionParameters() {
		return (List<? extends TextCommandParameter>) super.getOptionParameters();
	}

	public void execute(BContextImpl context, GuildMessageReceivedEvent event, String args, Matcher matcher) throws Exception {
		List<Object> objects = new ArrayList<>(parameters.size() + 1) {{
			if (isRegexCommand()) {
				add(new BaseCommandEventImpl(context, event, args));
			} else {
				add(new CommandEventImpl(context, event, args));
			}
		}};

		if (isRegexCommand()) {
			int groupIndex = 1;
			for (TextCommandParameter parameter : parameters) {
				if (parameter.isOption()) {
					int found = 0;

					final int groupCount = parameter.getGroupCount();
					final String[] groups = new String[groupCount];
					for (int j = 0; j < groupCount; j++) {
						groups[j] = matcher.group(groupIndex++);

						if (groups[j] != null) found++;
					}

					if (found == groupCount) { //Found all the groups
						objects.add(parameter.getResolver().resolve(event, groups));
					} else if (!parameter.isOptional()) {
						LOGGER.warn("Could not resolve parameter #{} in {} for input args {}",
								parameter.getIndex(),
								Utils.formatMethodShort(commandMethod),
								args);

						return;
					} else { //Parameter is optional
						if (parameter.isPrimitive()) {
							objects.add(0);
						} else {
							objects.add(null);
						}
					}
				} else {
					objects.add(parameter.getCustomResolver().resolve(event));
				}
			}
		} else {
			for (TextCommandParameter parameter : parameters) {
				objects.add(parameter.getCustomResolver().resolve(event));
			}
		}

		applyCooldown(event);

		//For some reason using an array list instead of a regular array
		// magically unboxes primitives when passed to Method#invoke
		commandMethod.invoke(getInstance(), objects.toArray());
	}

	public boolean isRegexCommand() {
		return getCompletePattern() != null;
	}
}