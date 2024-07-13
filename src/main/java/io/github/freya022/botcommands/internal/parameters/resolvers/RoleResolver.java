package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo;
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.text.TextCommandOption;
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver;
import io.github.freya022.botcommands.internal.utils.ExceptionsKt;
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

    private static final Pattern PATTERN = Pattern.compile("<@&(\\d+)>|(\\d+)");

    public RoleResolver() {
        super(Role.class);
    }

    @Nullable
    @Override
    public Role resolve(@NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @Nullable String @NotNull [] args) {
        final var id = args[0] != null ? args[0] : args[1];
        if (id == null) {
            ExceptionsKt.throwInternal("How can it not have either");
            return null; //Nope
        }
        if (event.getGuild().getId().equals(id)) return null; //@everyone role

        return event.getGuild().getRoleById(id);
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
    public String getHelpExample(@NotNull TextCommandOption option, @NotNull BaseCommandEvent event) {
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

    @Nullable
    @Override
    public Role resolve(@NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
        Objects.requireNonNull(event.getGuild(), "Can't get a role from DMs");

        return event.getGuild().getRoleById(arg);
    }
}
