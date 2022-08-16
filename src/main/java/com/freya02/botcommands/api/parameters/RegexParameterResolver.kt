package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.annotations.api.annotations.Optional;
import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
public interface RegexParameterResolver {
    /**
     * Returns a resolved object from this text command interaction
     *
     * @param context The {@link BContext} of this bot
     * @param info    The text command info of the command being executed
     * @param event   The event of this received message
     * @param args    The text arguments of this command, extracted with {@link #getPattern()}
     * @return The resolved option mapping
     */
    @Nullable
    Object resolve(@NotNull BContext context, @NotNull TextCommandInfo info, @NotNull MessageReceivedEvent event, @Nullable String[] args); //TODO array is NOT NULL, elements are nullable

    /**
     * Returns the pattern required to recognize this parameter
     * <br>This is used to compose a larger pattern which will represent an entire command syntax
     *
     * @return The {@link Pattern} for this parameter
     */
    @NotNull
    Pattern getPattern();

    /**
     * Returns an example string for this parameter
     * <br>This is only used to construct an example command internally and test the whole command's regex against it
     * <br>If the regex does not match the constructed example command, the framework will throw as the regex is deemed "too complex", see {@link Optional}
     *
     * @return An example string for validation purposes
     * @see Optional
     */
    @NotNull
    String getTestExample();

    default Pattern getPreferredPattern() {
        if (this instanceof QuotableRegexParameterResolver) {
            return ((QuotableRegexParameterResolver) this).getQuotedPattern();
        } else {
            return getPattern();
        }
    }
}