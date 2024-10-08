package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo;
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption;
import io.github.freya022.botcommands.api.components.options.ComponentOption;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.modals.ModalEvent;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.*;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@Resolver
public class StringResolver
        extends ClassParameterResolver<StringResolver, String>
        implements QuotableTextParameterResolver<StringResolver, String>,
                   SlashParameterResolver<StringResolver, String>,
                   ComponentParameterResolver<StringResolver, String>,
                   ModalParameterResolver<StringResolver, String>,
                   TimeoutParameterResolver<StringResolver, String> {

    public StringResolver() {
        super(String.class);
    }

    @Nullable
    @Override
    public String resolve(@NotNull TextCommandOption option, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
        return args[0];
    }

    @Override
    @NotNull
    public Pattern getPattern() {
        return Pattern.compile("(.+)");
    }

    @Override
    @NotNull
    public Pattern getQuotedPattern() {
        return Pattern.compile("\"(.+)\"");
    }

    @Override
    @NotNull
    public String getTestExample() {
        return "foobar";
    }

    @NotNull
    @Override
    public String getHelpExample(@NotNull TextCommandOption option, @NotNull BaseCommandEvent event) {
        return "foo bar";
    }

    @Override
    @NotNull
    public OptionType getOptionType() {
        return OptionType.STRING;
    }

    @Nullable
    @Override
    public String resolve(@NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
        return optionMapping.getAsString();
    }

    @Nullable
    @Override
    public String resolve(@NotNull GenericComponentInteractionCreateEvent event, @NotNull ComponentOption option, @NotNull String arg) {
        return arg;
    }

    @Nullable
    @Override
    public String resolve(@NotNull ModalEvent event, @NotNull ModalMapping modalMapping) {
        return modalMapping.getAsString();
    }

    @NotNull
    @Override
    public String resolve(@NotNull String arg) {
        return arg;
    }
}