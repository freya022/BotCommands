package io.github.freya022.botcommands.api.commands.text;

import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation;
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder;
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider;
import io.github.freya022.botcommands.api.core.reflect.ParameterType;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Base class for <b>annotated</b> text commands.
 *
 * <p>You are not required to use this if you use {@link TextCommandProvider}
 *
 * @see JDATextCommandVariation @JDATextCommandVariation
 */
@NullMarked
public abstract class TextCommand implements TextCommandHelpConsumer, TextGeneratedValueSupplierProvider {
    /**
     * <p>Returns a detailed embed of what the command is, it is used by the internal {@code help} command</p>
     * <p>The {@code help} command will automatically set the embed title to be {@code Command '[command_name]'} but can be overridden</p>
     * <p>It will also set the embed's description to be the command's description, <b>you can override with {@link EmbedBuilder#setDescription(CharSequence)}</b></p>
     *
     * @return The EmbedBuilder to use as a detailed description
     *
     * @see TextCommandBuilder#setDetailedDescription(kotlin.jvm.functions.Function1) DSL equivalent
     */
    @Nullable
    public Consumer<EmbedBuilder> getDetailedDescription() {
        return null;
    }

    @Override
    public final void accept(EmbedBuilder builder) {
        Consumer<EmbedBuilder> consumer = getDetailedDescription();
        if (consumer != null) {
            consumer.accept(builder);
        }
    }

    @Override
    public TextGeneratedValueSupplier getGeneratedValueSupplier(CommandPath commandPath,
                                                                String optionName,
                                                                ParameterType parameterType) {
        throw new IllegalArgumentException("Option '%s' in command path '%s' is a generated option but no generated value supplier has been given".formatted(optionName, commandPath.getFullPath()));
    }
}
