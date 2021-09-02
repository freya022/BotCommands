package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;
import java.util.Objects;

public class TextChannelResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public TextChannelResolver() {
		super(TextChannel.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return event.getGuild().getTextChannelById(args[0]);
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionMapping) {
		return optionMapping.getAsGuildChannel();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a guild from DMs");

		return event.getGuild().getTextChannelById(arg);
	}
}
