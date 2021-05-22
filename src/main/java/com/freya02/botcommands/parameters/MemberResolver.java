package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Objects;

public class MemberResolver extends ParameterResolver {
	@Override
	public boolean isRegexCommandSupported() {
		return true;
	}

	@Override
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		try {
			return event.getGuild().retrieveMemberById(args[0]).complete();
		} catch (ErrorResponseException e) {
			LOGGER.error("Could not resolve member in {} ({}): {}", event.getGuild().getName(), event.getGuild().getIdLong(), e.getMeaning());
			return null;
		}
	}

	@Override
	public boolean isSlashCommandSupported() {
		return true;
	}

	@Override
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return optionData.getAsMember();
	}

	@Override
	public boolean isButtonSupported() {
		return true;
	}

	@Override
	public Object resolve(ButtonClickEvent event, String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a member from DMs");

		try {
			return event.getGuild().retrieveMemberById(arg).complete();
		} catch (ErrorResponseException e) {
			LOGGER.error("Could not resolve member in {} ({}): {}", event.getGuild().getName(), event.getGuild().getIdLong(), e.getMeaning());
			return null;
		}
	}
}
