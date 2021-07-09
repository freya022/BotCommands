package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

public class UserResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public UserResolver() {
		super(User.class);
	}

	@Override
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return resolveUser(event.getJDA(), args[0]);
	}

	@Override
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return optionData.getAsUser();
	}

	@Override
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		return resolveUser(event.getJDA(), arg);
	}

	@Nullable
	private Object resolveUser(JDA jda, String arg) {
		try {
			return jda.retrieveUserById(arg).complete();
		} catch (ErrorResponseException e) {
			LOGGER.error("Could not resolve user: {}", e.getMeaning());
			return null;
		}
	}
}
