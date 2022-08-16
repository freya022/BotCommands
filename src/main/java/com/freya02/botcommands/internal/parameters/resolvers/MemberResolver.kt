package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.*;
import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import com.freya02.botcommands.internal.prefixed.TextUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

@IncludeClasspath
public class MemberResolver
		extends ParameterResolver<MemberResolver, Member>
		implements RegexParameterResolver<MemberResolver, Member>,
		           SlashParameterResolver<MemberResolver, Member>,
		           ComponentParameterResolver<MemberResolver, Member>,
		           UserContextParameterResolver<MemberResolver, Member> {

	private static final Pattern PATTERN = Pattern.compile("(?:<@!?)?(\\d+)>?");

	public MemberResolver() {
		super(Member.class);
	}

	@Override
	@Nullable
	public Member resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		try {
			//Fastpath for mentioned entities passed in the message
			long id = Long.parseLong(args[0]);

			return TextUtils.findEntity(id,
					event.getMessage().getMentions().getMembers(),
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
	@NotNull
	public OptionType getOptionType() {
		return OptionType.USER;
	}

	@Override
	@Nullable
	public Member resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsMember();
	}

	@Override
	@Nullable
	public Member resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
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
	public Member resolve(@NotNull BContext context, @NotNull UserCommandInfo info, @NotNull UserContextInteractionEvent event) {
		return event.getTargetMember();
	}
}
