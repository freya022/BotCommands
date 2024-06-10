package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo;
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver;
import io.github.freya022.botcommands.internal.components.handler.ComponentDescriptor;
import kotlin.reflect.KParameter;
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

/**
 * @see RoleResolverFactoryProvider
 */
public class RoleResolver
		extends ClassParameterResolver<RoleResolver, Role>
		implements TextParameterResolver<RoleResolver, Role>,
		           SlashParameterResolver<RoleResolver, Role>,
		           ComponentParameterResolver<RoleResolver, Role> {

	private static final Pattern PATTERN = Pattern.compile("(?:<@&)?(\\d+)>?");

	public RoleResolver() {
		super(Role.class);
	}

	@Nullable
	@Override
	public Role resolve(@NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
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

	@NotNull
	@Override
	public String getHelpExample(@NotNull KParameter parameter, @NotNull BaseCommandEvent event, boolean isID) {
		return event.getMember().getRoles().stream().findAny()
				.or(() -> event.getGuild().getRoleCache().streamUnordered().findAny())
				.map(Role::getAsMention)
				.orElse("role-id/mention");
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.ROLE;
	}

	@Nullable
    @Override
    public Role resolve(@NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsRole();
	}

	@Override
	@Nullable
	public Role resolve(@NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		Objects.requireNonNull(event.getGuild(), "Can't get a role from DMs");

		return event.getGuild().getRoleById(arg);
	}
}
