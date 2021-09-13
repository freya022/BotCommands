package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class EmoteResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public EmoteResolver() {
		super(Emote.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		return getEmoteInGuild(args[1], event.getGuild());
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionMapping) {
		final Guild guild = event.getGuild();

		if (guild != null) {
			return getEmoteInGuild(optionMapping.getAsString(), guild);
		} else {
			return event.getJDA().getEmoteById(optionMapping.getAsString());
		}
	}

	@Override
	@Nullable
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
