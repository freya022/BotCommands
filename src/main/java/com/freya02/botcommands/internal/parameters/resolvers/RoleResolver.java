package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

@IncludeClasspath
public class RoleResolver
		extends ParameterResolver<RoleResolver, Role>
		implements RegexParameterResolver<RoleResolver, Role>,
		           SlashParameterResolver<RoleResolver, Role>,
		           ComponentParameterResolver<RoleResolver, Role> {

	private static final Pattern PATTERN = Pattern.compile("(?:<@&)?(\\d+)>?");

	public RoleResolver() {
		super(Role.class);
	}

	@Override
	@Nullable
	public Role resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
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
	@NotNull
	public OptionType getOptionType() {
		return OptionType.ROLE;
	}

	@Override
	@Nullable
	public Role resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsRole();
	}

	@Override
	@Nullable
	public Role resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a role from DMs");

		return event.getGuild().getRoleById(arg);
	}
}
