package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.parameters.*;
import com.freya02.botcommands.internal.prefixed.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

public class MemberResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver, UserContextParameterResolver {
	private static final Pattern PATTERN = Pattern.compile("(?:<@!?)?(\\d+)>?");

	public MemberResolver() {
		super(Member.class);
	}

	@Override
	@Nullable
	public Object resolve(MessageReceivedEvent event, String[] args) {
		try {
			//Fastpath for mentioned entities passed in the message
			long id = Long.parseLong(args[0]);

			return Utils.findEntity(id,
					event.getMessage().getMentionedMembers(),
					() -> event.getGuild().retrieveMemberById(id).complete());
		} catch (ErrorResponseException e) {
			LOGGER.debug("Could not resolve member in {} ({}): {} (regex command, may not be an error)", event.getGuild().getName(), event.getGuild().getIdLong(), e.getMeaning());
			return null;
		}
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return PATTERN;
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "<@1234>";
	}

	@Override
	@Nullable
	public Object resolve(CommandInteractionPayload event, OptionMapping optionMapping) {
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
	public Object resolve(UserContextInteractionEvent event) {
		return event.getTargetMember();
	}
}
