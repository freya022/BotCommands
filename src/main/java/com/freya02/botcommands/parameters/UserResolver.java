package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

public class UserResolver extends ParameterResolver {
	@Override
	public boolean isRegexCommandSupported() {
		return true;
	}

	@Override
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return resolveUser(event.getJDA(), args[0]);
	}

	@Override
	public boolean isSlashCommandSupported() {
		return true;
	}

	@Override
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return optionData.getAsUser();
	}

	@Override
	public boolean isButtonSupported() {
		return true;
	}

	@Override
	public Object resolve(ButtonClickEvent event, String arg) {
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
