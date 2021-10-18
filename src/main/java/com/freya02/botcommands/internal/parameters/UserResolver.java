package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.parameters.*;
import com.freya02.botcommands.internal.prefixed.Utils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.commands.UserContextCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class UserResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver, UserContextParameterResolver {
	private static final Pattern PATTERN = Pattern.compile("(?:<@!?)?(\\d+)>?");

	public UserResolver() {
		super(User.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		try {
			//Fastpath for mentioned entities passed in the message
			long id = Long.parseLong(args[0]);

			return Utils.findEntity(id,
					event.getMessage().getMentionedUsers(),
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
	@Nullable
	public Object resolve(SlashCommandEvent event, OptionMapping optionMapping) {
		return optionMapping.getAsUser();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		try {
			return event.getJDA().retrieveUserById(arg).complete();
		} catch (ErrorResponseException e) {
			LOGGER.error("Could not resolve user: {}", e.getMeaning());
			return null;
		}
	}

	@Nullable
	@Override
	public Object resolve(UserContextCommandEvent event) {
		return event.getTargetUser();
	}
}
