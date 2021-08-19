package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

public class MentionableResolver extends ParameterResolver implements SlashParameterResolver {
	public MentionableResolver() {
		super(IMentionable.class);
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return optionData.getAsMentionable();
	}
}
