package io.github.freya022.botcommands.api.commands.text;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A consumer that's called when a help embed is about to be sent.
 * <br>That embed can be for the command list as well as individual commands.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}.
 *
 * @see #acceptGlobal(EmbedBuilder)
 * @see #acceptCommand(EmbedBuilder, TextCommandInfo)
 * @see InterfacedService @InterfacedService
 */
@NullMarked
@InterfacedService(acceptMultiple = false)
public interface HelpBuilderConsumer {
    /**
     * The function called when building a help embed
     *
     * @param builder     The {@link EmbedBuilder} to fill / override
     * @param isGlobal    {@code true} if the embed is showing all the commands,
     *                    {@code false} if the embed is for a specific command
     * @param commandInfo The text command to retrieve the help from
     *                    <br>Will be null if {@code isGlobal} is {@code true}
     *
     * @deprecated Replaced by {@link #acceptGlobal(EmbedBuilder)} and {@link #acceptCommand(EmbedBuilder, TextCommandInfo)}
     */
    @Deprecated(forRemoval = true)
    default void accept(EmbedBuilder builder, boolean isGlobal, @Nullable TextCommandInfo commandInfo) {
        if (isGlobal) {
            acceptGlobal(builder);
        } else {
            acceptCommand(builder, Objects.requireNonNull(commandInfo));
        }
    }

    /**
     * Customizes the given {@link EmbedBuilder} when showing help for all commands.
     *
     * @param builder The embed to customize
     */
    default void acceptGlobal(EmbedBuilder builder) {
        accept(builder, true, null);
    }

    /**
     * Customizes the given {@link EmbedBuilder} when showing help for a specific command.
     *
     * @param builder     The embed to customize
     * @param commandInfo The command to customize the embed for
     */
    default void acceptCommand(EmbedBuilder builder, TextCommandInfo commandInfo) {
        accept(builder, false, commandInfo);
    }
}
