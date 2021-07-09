package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LongResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public LongResolver() {
		super(long.class);
	}

	@Override
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return Long.valueOf(args[0]);
	}

	@Override
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return optionData.getAsLong();
	}

	@Override
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return Long.valueOf(arg);
	}
}
