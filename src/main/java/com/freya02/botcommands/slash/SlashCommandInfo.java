package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Cooldownable;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.annotation.Optional;
import com.freya02.botcommands.annotation.RequireOwner;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.slash.annotations.Option;
import com.freya02.botcommands.slash.impl.SlashEventImpl;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class SlashCommandInfo extends Cooldownable {
	private static final Logger LOGGER = Logging.getLogger();
	/** This is NOT localized */
	private final String name, description;
	private final String baseName;
	private final boolean guildOnly;

	private final EnumSet<Permission> userPermissions = EnumSet.noneOf(Permission.class);
	private final EnumSet<Permission> botPermissions = EnumSet.noneOf(Permission.class);
	private final boolean ownerOnly;

	private final SlashCommand instance;
	private final Method commandMethod;
	private final SlashCommandParameter[] commandParameters;

	/** This is NOT localized */
	private final String path;

	private int pathComponents = 1;

	public SlashCommandInfo(SlashCommand slashCommand, Method commandMethod) {
		super(commandMethod.getAnnotation(JdaSlashCommand.class).cooldownScope(), commandMethod.getAnnotation(JdaSlashCommand.class).cooldown());

		final JdaSlashCommand annotation = commandMethod.getAnnotation(JdaSlashCommand.class);

		this.instance = slashCommand;
		this.commandMethod = commandMethod;
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
		
		if (annotation.subcommand().isEmpty()) {
			this.name = annotation.name();
		} else {
			this.name = annotation.subcommand();
		}

		this.description = annotation.description();

		this.guildOnly = annotation.guildOnly();

		if ((annotation.userPermissions().length != 0 || annotation.botPermissions().length != 0) && !annotation.guildOnly())
			throw new IllegalArgumentException(commandMethod + " : slash command with permissions should be guild-only");

		Collections.addAll(userPermissions, annotation.userPermissions());
		Collections.addAll(botPermissions, annotation.botPermissions());

		this.ownerOnly = commandMethod.isAnnotationPresent(RequireOwner.class);
	}

	public int getPathComponents() {
		return pathComponents;
	}

	public SlashCommand getInstance() {
		return instance;
	}

	public Method getCommandMethod() {
		return commandMethod;
	}

	/** This is NOT localized */
	public String getName() {
		return name;
	}

	public boolean isGuildOnly() {
		return guildOnly;
	}

	/** This is NOT localized */
	public String getDescription() {
		return description;
	}

	public EnumSet<Permission> getUserPermissions() {
		return userPermissions;
	}

	public EnumSet<Permission> getBotPermissions() {
		return botPermissions;
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

			for (int i = 0, commandParametersLength = commandParameters.length; i < commandParametersLength; i++) {
				SlashCommandParameter parameter = commandParameters[i];
				
				final OptionMapping optionData = event.getOptions().get(i);

				if (optionData == null) {
					if (parameter.isOptional()) {
						if (parameter.getType().isPrimitive()) {
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

	public boolean isOwnerOnly() {
		return ownerOnly;
	}

	public String getBaseName() {
		return baseName;
	}
}
