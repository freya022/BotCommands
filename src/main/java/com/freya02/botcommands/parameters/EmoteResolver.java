package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

public class EmoteResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public EmoteResolver() {
		super(Emote.class);
	}

	@Override
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return getEmoteInGuild(args[1], event.getGuild());
	}

	@Override
	public Object resolve(SlashCommandEvent event, OptionMapping optionData) {
		final Guild guild = event.getGuild();

		if (guild != null) {
			return getEmoteInGuild(optionData.getAsString(), guild);
		} else {
			return event.getJDA().getEmoteById(optionData.getAsString());
		}
	}

	@Override
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		final Guild guild = event.getGuild();

		if (guild != null) {
			return getEmoteInGuild(arg, guild);
		} else {
			return event.getJDA().getEmoteById(arg);
		}
	}

	@Nullable
	private Object getEmoteInGuild(String arg, Guild guild) {
		try {
			return guild.retrieveEmoteById(arg).complete();
		} catch (ErrorResponseException e) {
			LOGGER.error("Could not resolve emote in {} ({}): {}", guild.getName(), guild.getIdLong(), e.getMeaning());
			return null;
		}
	}
}
