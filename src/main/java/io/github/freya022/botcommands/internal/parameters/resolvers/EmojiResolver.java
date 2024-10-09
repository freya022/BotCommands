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
import io.github.freya022.botcommands.api.utils.EmojiUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Resolver
public class EmojiResolver
        extends ClassParameterResolver<EmojiResolver, Emoji>
        implements TextParameterResolver<EmojiResolver, Emoji>,
                   SlashParameterResolver<EmojiResolver, Emoji>,
                   ComponentParameterResolver<EmojiResolver, Emoji>,
                   TimeoutParameterResolver<EmojiResolver, Emoji> {

    public EmojiResolver() {
        super(Emoji.class);
    }

    @Nullable
    @Override
    public Emoji resolve(@NotNull TextCommandOption option, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
        return getEmoji(args[0]);
    }

    @Override
    @NotNull
    public Pattern getPattern() {
        return Pattern.compile("(\\S+)");
    }

    @Override
    @NotNull
    public String getTestExample() {
        return "<:name:1234>";
    }

    @NotNull
    @Override
    public String getHelpExample(@NotNull TextCommandOption option, @NotNull BaseCommandEvent event) {
        return ":joy:";
    }

    @Override
    @NotNull
    public OptionType getOptionType() {
        return OptionType.STRING;
    }

    @Nullable
    @Override
    public Emoji resolve(@NotNull SlashCommandOption option, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
        return getEmoji(optionMapping.getAsString());
    }

    @Nullable
    @Override
    public Emoji resolve(@NotNull GenericComponentInteractionCreateEvent event, @NotNull ComponentOption option, @NotNull String arg) {
        return getEmoji(arg);
    }

    @Nullable
    @Override
    public Emoji resolve(@NotNull TimeoutOption option, @NotNull String arg) {
        return getEmoji(arg);
    }

    @Nullable
    private Emoji getEmoji(String arg) {
        final Matcher emoteMatcher = Message.MentionType.EMOJI.getPattern().matcher(arg);
        if (emoteMatcher.find()) {
            return Emoji.fromCustom(emoteMatcher.group(1), Long.parseUnsignedLong(emoteMatcher.group(2)), arg.startsWith("<a"));
        } else {
            try {
                return EmojiUtils.resolveJDAEmoji(arg);
            } catch (NoSuchElementException e) {
                return null;
            }
        }
    }
}
