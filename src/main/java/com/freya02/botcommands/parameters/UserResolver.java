package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

public class UserResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public UserResolver() {
		super(User.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		try {
			return event.getJDA().retrieveUserById(args[0]).complete();
		} catch (ErrorResponseException e) {
			return null;
		}
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return optionData.getAsUser();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		try {
			return event.getJDA().retrieveUserById(arg).complete();
		} catch (ErrorResponseException e) {
			LOGGER.error("Could not resolve user: {}", e.getMeaning());
			return null;
		}
	}
}
