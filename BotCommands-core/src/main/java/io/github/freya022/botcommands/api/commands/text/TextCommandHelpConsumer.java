package io.github.freya022.botcommands.api.commands.text;

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jspecify.annotations.NonNull;

/**
 * Optional interface used by the built-in {@code help} command.
 * <br>This consumer will apply to all text commands present in a given class.
 *
 * <p>If you use your own help command, you do not need to implement this.
 */
public interface TextCommandHelpConsumer {
    /**
     * Customizes the provided embed builder.
     *
     * @see TextCommandBuilder#setDetailedDescription(kotlin.jvm.functions.Function1) DSL equivalent
     */
    void accept(@NonNull EmbedBuilder builder);
}
