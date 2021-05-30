package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Cooldownable;
import com.freya02.botcommands.annotation.Optional;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.slash.annotations.Option;
import com.freya02.botcommands.slash.impl.SlashEventImpl;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class SlashCommandInfo extends Cooldownable {
	private final String name, description, category;
	private final boolean guildOnly;

	private final EnumSet<Permission> userPermissions = EnumSet.noneOf(Permission.class);
	private final EnumSet<Permission> botPermissions = EnumSet.noneOf(Permission.class);

	private final SlashCommand instance;
	private final Method commandMethod;
	private final SlashCommandParameter[] commandParameters;

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

		this.path = pathBuilder.toString();

		if (annotation.subcommand().isEmpty()) {
			this.name = annotation.name();
		} else {
			this.name = annotation.subcommand();
		}

		this.description = annotation.description();
		this.category = annotation.category();

		this.guildOnly = annotation.guildOnly();

		if ((annotation.userPermissions().length != 0 || annotation.botPermissions().length != 0) && !annotation.guildOnly())
			throw new IllegalArgumentException(commandMethod + " : slash command with permissions should be guild-only");

		Collections.addAll(userPermissions, annotation.userPermissions());
		Collections.addAll(botPermissions, annotation.botPermissions());
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

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public boolean isGuildOnly() {
		return guildOnly;
	}

	public String getDescription() {
		return description;
	}

	public EnumSet<Permission> getUserPermissions() {
		return userPermissions;
	}

	public EnumSet<Permission> getBotPermissions() {
		return botPermissions;
	}

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

			for (SlashCommandParameter parameter : commandParameters) {
				final OptionMapping optionData = event.getOption(parameter.getEffectiveName());

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
				if (!parameter.getType().isAssignableFrom(obj.getClass())) {
					event.replyFormat("The parameter '%s' is not a valid type (expected a %s)", parameter.getEffectiveName(), parameter.getType().getSimpleName())
							.setEphemeral(true)
							.queue();

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
}
