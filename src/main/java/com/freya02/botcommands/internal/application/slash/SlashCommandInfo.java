package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SlashCommandInfo extends ApplicationCommandInfo {
	private static final Logger LOGGER = Logging.getLogger();
	/**
	 * This is NOT localized
	 */
	private final String description;

	private final MethodParameters<SlashCommandParameter> commandParameters;

	public SlashCommandInfo(BContext context, ApplicationCommand instance, Method commandMethod) {
		super(context, instance, commandMethod.getAnnotation(JDASlashCommand.class),
				commandMethod,
				commandMethod.getAnnotation(JDASlashCommand.class).name(),
				commandMethod.getAnnotation(JDASlashCommand.class).group(),
				commandMethod.getAnnotation(JDASlashCommand.class).subcommand());

		final JDASlashCommand annotation = commandMethod.getAnnotation(JDASlashCommand.class);

		this.commandParameters = MethodParameters.of(context, commandMethod, (parameter, i) -> {
			final Class<?> type = parameter.getType();

			if (Member.class.isAssignableFrom(type)
					|| Role.class.isAssignableFrom(type)
					|| GuildChannel.class.isAssignableFrom(type)) {
				if (!annotation.guildOnly())
					throw new IllegalArgumentException("The slash command " + Utils.formatMethodShort(commandMethod) + " cannot have a " + type.getSimpleName() + " parameter as it is not guild-only");
			}

			return new SlashCommandParameter(parameter, i);
		});

		if (!annotation.group().isBlank() && annotation.subcommand().isBlank())
			throw new IllegalArgumentException("Command group for " + Utils.formatMethodShort(commandMethod) + " is present but has no subcommand");

		this.description = annotation.description();
	}

	/**
	 * This is NOT localized
	 */
	public String getDescription() {
		return description;
	}

	public boolean execute(BContextImpl context, SlashCommandInteractionEvent event, Consumer<Throwable> throwableConsumer) throws Exception {
		List<Object> objects = new ArrayList<>(commandParameters.size() + 1) {{
			if (guildOnly) {
				add(new GuildSlashEvent(context, getMethod(), event));
			} else {
				add(new GlobalSlashEventImpl(context, getMethod(), event));
			}
		}};

		for (final SlashCommandParameter parameter : commandParameters) {
			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			final Object obj;
			if (parameter.isOption()) {
				String optionName = applicationOptionData.getEffectiveName();

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
						throw new RuntimeException("Slash parameter couldn't be resolved for method " + Utils.formatMethodShort(commandMethod) + " at parameter " + applicationOptionData.getEffectiveName() + " (" + optionName + ")");
					}
				}

				obj = parameter.getResolver().resolve(context, this, event, optionMapping);

				if (obj == null) {
					event.reply(context.getDefaultMessages(event.getUserLocale()).getSlashCommandUnresolvableParameterMsg(applicationOptionData.getEffectiveName(), parameter.getBoxedType().getSimpleName()))
							.setEphemeral(true)
							.queue();

					//Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
					LOGGER.trace("The parameter '{}' of value '{}' could not be resolved into a {}", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return false;
				}

				if (!parameter.getBoxedType().isAssignableFrom(obj.getClass())) {
					event.reply(context.getDefaultMessages(event.getUserLocale()).getSlashCommandInvalidParameterTypeMsg(applicationOptionData.getEffectiveName(), parameter.getBoxedType().getSimpleName(), obj.getClass().getSimpleName()))
							.setEphemeral(true)
							.queue();

					LOGGER.error("The parameter '{}' of value '{}' is not a valid type (expected a {})", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return false;
				}
			} else {
				obj = parameter.getCustomResolver().resolve(context, this, event);
			}

			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			objects.add(obj);
		}

		applyCooldown(event);

		getMethodRunner().invoke(objects.toArray(), throwableConsumer);

		return true;
	}

	@Nullable
	public String getAutocompletionHandlerName(CommandAutoCompleteInteractionEvent event) {
		final AutoCompleteQuery autoCompleteQuery = event.getFocusedOption();

		for (final SlashCommandParameter parameter : commandParameters) {
			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			if (parameter.isOption()) {
				final String optionName = applicationOptionData.getEffectiveName();

				if (optionName.equals(autoCompleteQuery.getName())) {
					return applicationOptionData.getAutocompletionHandlerName();
				}
			}
		}

		return null;
	}

	@Override
	@NotNull
	public MethodParameters<SlashCommandParameter> getParameters() {
		return commandParameters;
	}

	@SuppressWarnings("unchecked")
	@Override
	@NotNull
	public List<? extends SlashCommandParameter> getOptionParameters() {
		return (List<? extends SlashCommandParameter>) super.getOptionParameters();
	}
}