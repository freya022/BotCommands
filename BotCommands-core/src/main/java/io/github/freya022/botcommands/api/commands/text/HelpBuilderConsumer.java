package io.github.freya022.botcommands.api.commands.text;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jspecify.annotations.NullMarked;

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
     * Customizes the given {@link EmbedBuilder} when showing help for all commands.
     *
     * @param builder The embed to customize
     */
    void acceptGlobal(EmbedBuilder builder);

    /**
     * Customizes the given {@link EmbedBuilder} when showing help for a specific command.
     *
     * @param builder     The embed to customize
     * @param commandInfo The command to customize the embed for
     */
    void acceptCommand(EmbedBuilder builder, TextCommandInfo commandInfo);
}
