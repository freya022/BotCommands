package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption;
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption;
import io.github.freya022.botcommands.api.components.options.ComponentOption;
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
public class DoubleResolver
        extends ClassParameterResolver<DoubleResolver, Double>
        implements TextParameterResolver<DoubleResolver, Double>,
                   SlashParameterResolver<DoubleResolver, Double>,
                   ComponentParameterResolver<DoubleResolver, Double>,
                   TimeoutParameterResolver<DoubleResolver, Double> {

    public DoubleResolver() {
        super(Double.class);
    }

    @Nullable
    @Override
    public Double resolve(@NotNull TextCommandOption option, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
        try {
            return Double.valueOf(args[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @NotNull
    public Pattern getPattern() {
        return Pattern.compile("([-+]?[0-9]*[.,]?[0-9]+)");
    }

    @Override
    @NotNull
    public String getTestExample() {
        return "1234.42";
    }

    @NotNull
    @Override
    public String getHelpExample(@NotNull TextCommandOption option, @NotNull BaseCommandEvent event) {
        return "3.14159";
    }

    @Override
    @NotNull
    public OptionType getOptionType() {
        return OptionType.NUMBER;
    }

    @Nullable
    @Override
    public Double resolve(@NotNull SlashCommandOption option, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
        try {
            return optionMapping.getAsDouble();
        } catch (NumberFormatException e) { //Can't have discord to send us actual input when autocompleting lmao
            return 0d;
        }
    }

    @Nullable
    @Override
    public Double resolve(@NotNull GenericComponentInteractionCreateEvent event, @NotNull ComponentOption option, @NotNull String arg) {
        return Double.valueOf(arg);
    }

    @Nullable
    @Override
    public Double resolve(@NotNull String arg) {
        return Double.valueOf(arg);
    }
}
