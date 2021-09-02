package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

public class DoubleResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public DoubleResolver() {
		super(Double.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return Double.valueOf(args[0]);
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionMapping) {
		return Double.valueOf(optionMapping.getAsString());
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return Double.valueOf(arg);
	}
}
