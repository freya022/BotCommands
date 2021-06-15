package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class MentionableResolver extends ParameterResolver implements SlashParameterResolver {
	public MentionableResolver() {
		super(IMentionable.class);
	}

	@Override
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return optionData.getAsMentionable();
	}
}
