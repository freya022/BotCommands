package com.freya02.botcommands.application.slash;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.annotation.Optional;
import com.freya02.botcommands.application.ApplicationCommandInfo;
import com.freya02.botcommands.application.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.application.slash.annotations.Option;
import com.freya02.botcommands.application.slash.impl.SlashEventImpl;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommandInfo extends ApplicationCommandInfo {
	private static final Logger LOGGER = Logging.getLogger();
	/** This is NOT localized */
	private final String description;
	private final String baseName;

	private final SlashCommand instance;
	private final SlashCommandParameter[] commandParameters;

	/** This is NOT localized */
	private final String path;

	private int pathComponents = 1;
	
	/** guild id => localized option names */
	private final Map<Long, List<String>> localizedOptionMap = new HashMap<>();

	public SlashCommandInfo(SlashCommand slashCommand, Method commandMethod) {
		super(commandMethod.getAnnotation(JdaSlashCommand.class),
				commandMethod.getAnnotation(JdaSlashCommand.class).subcommand().isEmpty() 
						? commandMethod.getAnnotation(JdaSlashCommand.class).name() 
						: commandMethod.getAnnotation(JdaSlashCommand.class).subcommand(),
				commandMethod);

		final JdaSlashCommand annotation = commandMethod.getAnnotation(JdaSlashCommand.class);

		this.instance = slashCommand;
		this.commandParameters = new SlashCommandParameter[commandMethod.getParameterCount() - 1];

		for (int i = 1, parametersLength = commandMethod.getParameterCount(); i < parametersLength; i++) {
			final Parameter parameter = commandMethod.getParameters()[i];
			final boolean optional = parameter.isAnnotationPresent(Optional.class);
			final Class<?> type = parameter.getType();
			final String name;

			final Option option = parameter.getAnnotation(Option.class);
			if (option == null) {
				name = parameter.getName();
			} else {
				if (option.name().isBlank()) {
					name = parameter.getName();
				} else {
					name = option.name();
				}
			}

			if (Member.class.isAssignableFrom(type)
					|| Role.class.isAssignableFrom(type)
					|| GuildChannel.class.isAssignableFrom(type) ) {
				if (!annotation.guildOnly())
					throw new IllegalArgumentException("The slash command " + commandMethod + " cannot have a " + type.getSimpleName() + " parameter as it is not guild-only");
			}

			commandParameters[i - 1] = new SlashCommandParameter(optional, name, type);
		}

		if (annotation.name().isBlank()) throw new IllegalArgumentException("Command name for " + commandMethod + " is blank");
		if (!annotation.group().isBlank() && annotation.subcommand().isBlank()) throw new IllegalArgumentException("Command group for " + commandMethod + " is present but has no subcommand");

		final StringBuilder pathBuilder = new StringBuilder(annotation.name());
		if (!annotation.group().isBlank()) {
			pathBuilder.append('/').append(annotation.group());
			pathComponents++;
		}
		if (!annotation.subcommand().isBlank()) {
			pathBuilder.append('/').append(annotation.subcommand());
			pathComponents++;
		}

		this.baseName = annotation.name();
		this.path = pathBuilder.toString();

		this.description = annotation.description();
	}
	
	public void putLocalizedOptions(long guildId, @Nonnull List<String> optionNames) {
		localizedOptionMap.put(guildId, optionNames);
	}

	public int getPathComponents() {
		return pathComponents;
	}

	public SlashCommand getInstance() {
		return instance;
	}

	/** This is NOT localized */
	public String getDescription() {
		return description;
	}

	/** This is NOT localized */
	public String getPath() {
		return path;
	}

	public boolean execute(BContext context, SlashCommandEvent event) {
		try {
			List<Object> objects = new ArrayList<>(commandParameters.length) {{
				if (guildOnly) {
					add(new GuildSlashEvent(context, event));
				} else {
					add(new SlashEventImpl(context, event));
				}
			}};

			final List<String> optionNames = event.getGuild() != null ? localizedOptionMap.get(event.getGuild().getIdLong()) : null;
			for (int i = 0, commandParametersLength = commandParameters.length; i < commandParametersLength; i++) {
				SlashCommandParameter parameter = commandParameters[i];
				
				String optionName = optionNames == null ? parameter.getEffectiveName() : optionNames.get(i);
				if (optionName == null) {
					throw new IllegalArgumentException(String.format("Option name #%d (%s) could not be resolved for %s", i, parameter.getEffectiveName(), getCommandMethod()));
				}
				
				final OptionMapping optionData = event.getOption(optionName);

				if (optionData == null) {
					if (parameter.isOptional()) {
						if (parameter.isPrimitive()) {
							objects.add(0);
						} else {
							objects.add(null);
						}

						continue;
					} else {
						throw new RuntimeException("Slash parameter couldn't be resolved for method " + commandMethod + " at parameter " + parameter.getEffectiveName());
					}
				}

				final Object obj = parameter.getResolver().resolve(event, optionData);
				if (obj == null) {
					event.replyFormat(context.getDefaultMessages().getSlashCommandUnresolvableParameterMsg(), parameter.getEffectiveName(), parameter.getType().getSimpleName())
							.setEphemeral(true)
							.queue();

					LOGGER.warn("The parameter '{}' of value '{}' could not be resolved into a {}", parameter.getEffectiveName(), optionData.getAsString(), parameter.getType().getSimpleName());

					return false;
				}

				if (!parameter.getType().isAssignableFrom(obj.getClass())) {
					event.replyFormat(context.getDefaultMessages().getSlashCommandInvalidParameterTypeMsg(), parameter.getEffectiveName(), parameter.getType().getSimpleName(), obj.getClass().getSimpleName())
							.setEphemeral(true)
							.queue();

					LOGGER.error("The parameter '{}' of value '{}' is not a valid type (expected a {})", parameter.getEffectiveName(), optionData.getAsString(), parameter.getType().getSimpleName());

					return false;
				}

				//For some reason using an array list instead of a regular array
				// magically unboxes primitives when passed to Method#invoke
				objects.add(obj);
			}

			commandMethod.invoke(instance, objects.toArray());

			return true;
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public String getBaseName() {
		return baseName;
	}
}
