package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Objects;

public class TextChannelResolver extends ParameterResolver {
	@Override
	public boolean isRegexCommandSupported() {
		return true;
	}

	@Override
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return event.getGuild().getTextChannelById(args[0]);
	}

	@Override
	public boolean isSlashCommandSupported() {
		return true;
	}

	@Override
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		return optionData.getAsGuildChannel();
	}

	@Override
	public boolean isButtonSupported() {
		return true;
	}

	@Override
	public Object resolve(ButtonClickEvent event, String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a guild from DMs");

		return event.getGuild().getTextChannelById(arg);
	}
}
