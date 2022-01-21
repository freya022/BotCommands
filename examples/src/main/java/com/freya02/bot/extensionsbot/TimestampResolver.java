package com.freya02.bot.extensionsbot;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimestampResolver extends ParameterResolver implements SlashParameterResolver, ComponentParameterResolver {
	public TimestampResolver() {
		super(Timestamp.class);
	}

	@Nullable
	@Override
	public Object resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return getTimestamp(arg);
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Nullable
	@Override
	public Object resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return getTimestamp(optionMapping.getAsString());
	}

	@Nullable
	private Timestamp getTimestamp(String str) {
		try {
			return TimeFormat.parse(str);
		} catch (Exception e) {
			//probably not an error, could be someone who sent an erroneous timestamp, will get reported by the application commands and components handlers
			LOGGER.trace("Cannot resolve timestamp '{}'", str);

			return null;
		}
	}
}
