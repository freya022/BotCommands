package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommandInfo extends ApplicationCommandInfo {
	private static final Logger LOGGER = Logging.getLogger();
	/** This is NOT localized */
	private final String description;

	private final Object instance;
	private final MethodParameters<SlashCommandParameter> commandParameters;

	/** guild id => localized option names */
	private final Map<Long, List<String>> localizedOptionMap = new HashMap<>();

	public SlashCommandInfo(ApplicationCommand instance, Method commandMethod) {
		super(instance, commandMethod.getAnnotation(JDASlashCommand.class),
				commandMethod,
				commandMethod.getAnnotation(JDASlashCommand.class).name(),
				commandMethod.getAnnotation(JDASlashCommand.class).group(),
				commandMethod.getAnnotation(JDASlashCommand.class).subcommand());

		final JDASlashCommand annotation = commandMethod.getAnnotation(JDASlashCommand.class);

		this.instance = instance;
		this.commandParameters = MethodParameters.of(commandMethod, (parameter, i) -> {
			if (parameter.isAnnotationPresent(TextOption.class))
				throw new IllegalArgumentException(String.format("Slash command parameter #%d of %s#%s cannot be annotated with @TextOption", i, commandMethod.getDeclaringClass().getName(), commandMethod.getName()));

			final Class<?> type = parameter.getType();

			if (Member.class.isAssignableFrom(type)
					|| Role.class.isAssignableFrom(type)
					|| GuildChannel.class.isAssignableFrom(type) ) {
				if (!annotation.guildOnly())
					throw new IllegalArgumentException("The slash command " + Utils.formatMethodShort(commandMethod) + " cannot have a " + type.getSimpleName() + " parameter as it is not guild-only");
			}

			return new SlashCommandParameter(parameter, i);
		});

		if (!annotation.group().isBlank() && annotation.subcommand().isBlank()) throw new IllegalArgumentException("Command group for " + Utils.formatMethodShort(commandMethod) + " is present but has no subcommand");

		this.description = annotation.description();
	}
	
	public void putLocalizedOptions(long guildId, @NotNull List<String> optionNames) {
		localizedOptionMap.put(guildId, optionNames);
	}

	/** This is NOT localized */
	public String getDescription() {
		return description;
	}

	public boolean execute(BContext context, SlashCommandEvent event) throws Exception {
		List<Object> objects = new ArrayList<>(commandParameters.size() + 1) {{
			if (guildOnly) {
				add(new GuildSlashEvent(context, event));
			} else {
				add(new GlobalSlashEventImpl(context, event));
			}
		}};

		int optionIndex = 0;
		final List<String> optionNames = event.getGuild() != null ? localizedOptionMap.get(event.getGuild().getIdLong()) : null;
		for (final SlashCommandParameter parameter : commandParameters) {
			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			final Object obj;
			if (parameter.isOption()) {
				String optionName = optionNames == null ? applicationOptionData.getEffectiveName() : optionNames.get(optionIndex);
				if (optionName == null) {
					throw new IllegalArgumentException(String.format("Option name #%d (%s) could not be resolved for %s", optionIndex, applicationOptionData.getEffectiveName(), Utils.formatMethodShort(getCommandMethod())));
				}

				optionIndex++;

				final OptionMapping optionMapping = event.getOption(optionName);

				if (optionMapping == null) {
					if (parameter.isOptional()) {
						if (parameter.isPrimitive()) {
							objects.add(0);
						} else {
							objects.add(null);
						}

						continue;
					} else {
						throw new RuntimeException("Slash parameter couldn't be resolved for method " + Utils.formatMethodShort(commandMethod) + " at parameter " + applicationOptionData.getEffectiveName());
					}
				}

				obj = parameter.getResolver().resolve(event, optionMapping);

				if (obj == null) {
					event.replyFormat(context.getDefaultMessages(event.getGuild()).getSlashCommandUnresolvableParameterMsg(), applicationOptionData.getEffectiveName(), parameter.getBoxedType().getSimpleName())
							.setEphemeral(true)
							.queue();

					//Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
					LOGGER.trace("The parameter '{}' of value '{}' could not be resolved into a {}", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return false;
				}

				if (!parameter.getBoxedType().isAssignableFrom(obj.getClass())) {
					event.replyFormat(context.getDefaultMessages(event.getGuild()).getSlashCommandInvalidParameterTypeMsg(), applicationOptionData.getEffectiveName(), parameter.getBoxedType().getSimpleName(), obj.getClass().getSimpleName())
							.setEphemeral(true)
							.queue();

					LOGGER.error("The parameter '{}' of value '{}' is not a valid type (expected a {})", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return false;
				}
			} else {
				obj = parameter.getCustomResolver().resolve(event);
			}

			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			objects.add(obj);
		}

		applyCooldown(event);

		commandMethod.invoke(instance, objects.toArray());

		return true;
	}

	@Override
	public MethodParameters<SlashCommandParameter> getParameters() {
		return commandParameters;
	}
}