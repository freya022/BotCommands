package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class EmoteResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	public EmoteResolver() {
		super(Emote.class);
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		return getEmoteInGuild(args[1], event.getGuild());
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Message.MentionType.EMOTE.getPattern();
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "<:name:1234>";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		final Guild guild = event.getGuild();

		if (guild != null) {
			return getEmoteInGuild(optionMapping.getAsString(), guild);
		} else {
			return event.getJDA().getEmoteById(optionMapping.getAsString());
		}
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
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
