package com.freya02.bot.extensionsbot;

import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.CommandPayload;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;
import org.jetbrains.annotations.Nullable;

public class TimestampResolver extends ParameterResolver implements SlashParameterResolver, ComponentParameterResolver {
	public TimestampResolver() {
		super(Timestamp.class);
	}

	@Nullable
	@Override
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return getTimestamp(arg);
	}

	@Nullable
	@Override
	public Object resolve(CommandPayload event, OptionMapping optionData) {
		return getTimestamp(optionData.getAsString());
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
