package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

public class GuildResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public GuildResolver() {
		super(Guild.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return resolveGuild(event.getJDA(), args[0]);
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return resolveGuild(event.getJDA(), arg);
	}

	@Nullable
	private Guild resolveGuild(JDA jda, String arg) {
		return jda.getGuildById(arg);
	}
}
