package io.github.freya022.botcommands.api.commands.text;

import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption;
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation;
import io.github.freya022.botcommands.api.core.reflect.ParameterType;
import org.jspecify.annotations.NonNull;

/**
 * Interface to be implemented for {@link GeneratedOption @GeneratedOption}s of <b>annotated</b> text commands.
 *
 * <p>This must be implemented on the same class as the command.
 */
public interface TextGeneratedValueSupplierProvider {
    /**
     * Returns the generated value supplier of an {@link GeneratedOption @GeneratedOption}.
     *
     * <p>This method will only be called once per command option per guild.
     *
     * @param commandPath   The path of the command, as set in {@link JDATextCommandVariation}
     * @param optionName    The name of the <b>transformed</b> command option, might not be equal to the parameter name
     * @param parameterType The <b>boxed</b> type of the command option
     *
     * @return A {@link TextGeneratedValueSupplier} to generate the option on command execution
     */
    @NonNull
    TextGeneratedValueSupplier getGeneratedValueSupplier(@NonNull CommandPath commandPath,
                                                         @NonNull String optionName,
                                                         @NonNull ParameterType parameterType);
}
