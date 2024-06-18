package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo;
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.text.TextCommandOption;
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@Resolver
public class BooleanResolver
        extends ClassParameterResolver<BooleanResolver, Boolean>
        implements TextParameterResolver<BooleanResolver, Boolean>,
                   SlashParameterResolver<BooleanResolver, Boolean>,
                   ComponentParameterResolver<BooleanResolver, Boolean>,
                   TimeoutParameterResolver<BooleanResolver, Boolean> {

    public BooleanResolver() {
        super(Boolean.class);
    }

    @Nullable
    @Override
    public Boolean resolve(@NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
        return parseBoolean(args[0]);
    }

    @Override
    @NotNull
    public Pattern getPattern() {
        return Pattern.compile("(true|false)", Pattern.CASE_INSENSITIVE);
    }

    @Override
    @NotNull
    public String getTestExample() {
        return "true";
    }

    @NotNull
    @Override
    public String getHelpExample(@NotNull TextCommandOption option, @NotNull BaseCommandEvent event) {
        return "true";
    }

    @Override
    @NotNull
    public OptionType getOptionType() {
        return OptionType.BOOLEAN;
    }

    @Nullable
    @Override
    public Boolean resolve(@NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
        return optionMapping.getAsBoolean();
    }

    @Nullable
    @Override
    public Boolean resolve(@NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
        return parseBoolean(arg);
    }

    @Nullable
    @Override
    public Boolean resolve(@NotNull String arg) {
        return parseBoolean(arg);
    }

    @Nullable
    private Boolean parseBoolean(String arg) {
        if (arg.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        } else if (arg.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else {
            return null;
        }
    }
}
