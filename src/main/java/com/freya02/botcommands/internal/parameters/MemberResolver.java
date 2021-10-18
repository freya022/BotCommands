package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.parameters.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.commands.UserContextCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

public class MemberResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver, UserContextParameterResolver {
	public MemberResolver() {
		super(Member.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		try {
			return event.getGuild().retrieveMemberById(args[0]).complete();
		} catch (ErrorResponseException e) {
			LOGGER.debug("Could not resolve member in {} ({}): {} (regex command, may not be an error)", event.getGuild().getName(), event.getGuild().getIdLong(), e.getMeaning());
			return null;
		}
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Message.MentionType.USER.getPattern();
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "<@1234>";
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionMapping) {
		return optionMapping.getAsMember();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a member from DMs");

		try {
			return event.getGuild().retrieveMemberById(arg).complete();
		} catch (ErrorResponseException e) {
			LOGGER.error("Could not resolve member in {} ({}): {}", event.getGuild().getName(), event.getGuild().getIdLong(), e.getMeaning());
			return null;
		}
	}

	@Nullable
	@Override
	public Object resolve(UserContextCommandEvent event) {
		return event.getTargetMember();
	}
}
