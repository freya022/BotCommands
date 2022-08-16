package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.*;
import com.freya02.botcommands.core.api.annotations.BService;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import com.freya02.botcommands.internal.prefixed.TextUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@BService
public class UserResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver, UserContextParameterResolver {
	private static final Pattern PATTERN = Pattern.compile("(?:<@!?)?(\\d+)>?");

	public UserResolver() {
		super(ParameterType.ofClass(User.class));
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		try {
			//Fastpath for mentioned entities passed in the message
			long id = Long.parseLong(args[0]);

			return TextUtils.findEntity(id,
					event.getMessage().getMentions().getUsers(),
					() -> event.getJDA().retrieveUserById(id).complete());
		} catch (ErrorResponseException e) {
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
	public Object resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsUser();
	}

	@Override
	@Nullable
	public Object resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		try {
			return event.getJDA().retrieveUserById(arg).complete();
		} catch (ErrorResponseException e) {
			LOGGER.error("Could not resolve user: {}", e.getMeaning());
			return null;
		}
	}

	@Nullable
	@Override
	public Object resolve(@NotNull BContext context, @NotNull UserCommandInfo info, @NotNull UserContextInteractionEvent event) {
		return event.getTarget();
	}
}
