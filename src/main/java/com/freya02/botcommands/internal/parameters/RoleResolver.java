package com.freya02.botcommands.internal.parameters;

import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.interactions.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

public class RoleResolver extends ParameterResolver implements RegexParameterResolver, SlashParameterResolver, ComponentParameterResolver {
	private static final Pattern PATTERN = Pattern.compile("(?:<@&)?(\\d+)>?");

	public RoleResolver() {
		super(Role.class);
	}

	@Override
	@Nullable
	public Object resolve(GuildMessageReceivedEvent event, String[] args) {
		if (event.getGuild().getId().equals(args[0])) return null; //@everyone role

		return event.getGuild().getRoleById(args[0]);
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return PATTERN;
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "<@&1234>";
	}

	@Override
	@Nullable
	public Object resolve(SlashCommandInteraction event, OptionMapping optionMapping) {
		return optionMapping.getAsRole();
	}

	@Override
	@Nullable
	public Object resolve(GenericComponentInteractionCreateEvent event, String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a role from DMs");

		return event.getGuild().getRoleById(arg);
	}
}
