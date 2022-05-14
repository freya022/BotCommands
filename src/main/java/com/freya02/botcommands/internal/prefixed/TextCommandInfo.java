package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.builder.TextCommandBuilder;
import com.freya02.botcommands.internal.AbstractCommandInfo;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.freya02.botcommands.internal.prefixed.ExecutionResult.CONTINUE;
import static com.freya02.botcommands.internal.prefixed.ExecutionResult.OK;

public final class TextCommandInfo extends AbstractCommandInfo<TextCommand> {
	private static final Logger LOGGER = Logging.getLogger();
	private final List<CommandPath> aliases;

	private final String description;

	private final boolean hidden;

	private final MethodParameters<TextCommandParameter> parameters;

	private final Pattern completePattern;
	private final int order;

	public TextCommandInfo(@NotNull BContext context,
	                       @NotNull TextCommandBuilder builder) {
		super(context, builder);

		this.aliases = aliases;
		this.description = description;

		this.order = order;

//		final boolean isRegexMethod = !ReflectionUtils.hasFirstParameter(commandMethod, CommandEvent.class);
		parameters = MethodParameters.of(method, (index, parameter) -> {
//			if (parameter.isAnnotationPresent(AppOption.class))
//				throw new IllegalArgumentException(String.format("Text command parameter #%d of %s#%s cannot be annotated with @AppOption", index, commandMethod.getDeclaringClass().getName(), commandMethod.getName()));
//
//			//Fallback doesn't accept options
//			if (parameter.isAnnotationPresent(TextOption.class) && !isRegexMethod)
//				throw new IllegalArgumentException("Fallback text commands (CommandEvent ones) cannot have parameters annotated with @TextOption");

			return new TextCommandParameter(RegexParameterResolver.class, parameter, index);
		});

		this.hidden = hidden;

		if (parameters.getOptionCount() > 0) {
			completePattern = CommandPattern.of(this);
		} else completePattern = null;
	}

	public boolean isHidden() {
		return hidden;
	}

	public List<CommandPath> getAliases() {
		return aliases;
	}

	public boolean hasDescription() {
		return !description.isBlank();
	}

	@NotNull
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
	@NotNull
	public MethodParameters<TextCommandParameter> getParameters() {
		return parameters;
	}

	@SuppressWarnings("unchecked")
	@Override
	@NotNull
	public List<? super TextCommandParameter> getOptionParameters() {
		return (List<TextCommandParameter>) super.getOptionParameters();
	}

	public ExecutionResult execute(BContextImpl context, MessageReceivedEvent event, String args, Matcher matcher, Consumer<Throwable> throwableConsumer) throws Exception {
		List<Object> objects = new ArrayList<>(parameters.size() + 1) {{
			if (isRegexCommand()) {
				add(new BaseCommandEventImpl(context, getMethod(), event, args));
			} else {
				add(new CommandEventImpl(context, getMethod(), event, args));
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
						final Object resolved = parameter.getResolver().resolve(context, this, event, groups);
						//Regex matched but could not be resolved
						// if optional then it's ok
						if (resolved == null && !parameter.isOptional()) {
							return CONTINUE;
						}

						objects.add(resolved);
					} else if (!parameter.isOptional()) { //Parameter is not found yet the pattern matched and is not optional
						LOGGER.warn("Could not find parameter #{} in {} for input args {}",
								parameter.getIndex(),
								Utils.formatMethodShort(method),
								args);

						return CONTINUE;
					} else { //Parameter is optional
						if (parameter.isPrimitive()) {
							objects.add(0);
						} else {
							objects.add(null);
						}
					}
				} else {
					objects.add(parameter.getCustomResolver().resolve(context, this, event));
				}
			}
		} else {
			for (TextCommandParameter parameter : parameters) {
				objects.add(parameter.getCustomResolver().resolve(context, this, event));
			}
		}

		applyCooldown(event);

		//For some reason using an array list instead of a regular array
		// magically unboxes primitives when passed to Method#invoke
		getMethodRunner().invoke(objects.toArray(), throwableConsumer);

		return OK;
	}

	public boolean isRegexCommand() {
		return getCompletePattern() != null;
	}
}