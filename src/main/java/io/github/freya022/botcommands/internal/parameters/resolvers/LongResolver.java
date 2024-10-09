package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption;
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption;
import io.github.freya022.botcommands.api.components.options.ComponentOption;
import io.github.freya022.botcommands.api.components.timeout.options.TimeoutOption;
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
public class LongResolver
        extends ClassParameterResolver<LongResolver, Long>
        implements TextParameterResolver<LongResolver, Long>,
                   SlashParameterResolver<LongResolver, Long>,
                   ComponentParameterResolver<LongResolver, Long>,
                   TimeoutParameterResolver<LongResolver, Long> {

    public LongResolver() {
        super(Long.class);
    }

    @Nullable
    @Override
    public Long resolve(@NotNull TextCommandOption option, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
        try {
            return Long.valueOf(args[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @NotNull
    public Pattern getPattern() {
        return Pattern.compile("(\\d+)");
    }

    @Override
    @NotNull
    public String getTestExample() {
        return "1234";
    }

    @NotNull
    @Override
    public String getHelpExample(@NotNull TextCommandOption option, @NotNull BaseCommandEvent event) {
        return option.isId() ? "222046562543468545" : "42";
    }

    @Override
    @NotNull
    public OptionType getOptionType() {
        return OptionType.INTEGER;
    }

    @Nullable
    @Override
    public Long resolve(@NotNull SlashCommandOption option, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
        try {
            return optionMapping.getAsLong();
        } catch (NumberFormatException e) { //Can't have discord to send us actual input when autocompleting lmao
            return 0L;
        }
    }

    @Nullable
    @Override
    public Long resolve(@NotNull ComponentOption option, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
        return Long.valueOf(arg);
    }

    @Nullable
    @Override
    public Long resolve(@NotNull TimeoutOption option, @NotNull String arg) {
        return Long.valueOf(arg);
    }
}
