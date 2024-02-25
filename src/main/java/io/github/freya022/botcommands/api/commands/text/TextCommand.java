package io.github.freya022.botcommands.api.commands.text;

import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption;
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier;
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation;
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder;
import io.github.freya022.botcommands.api.commands.text.declaration.TextCommandsDeclaration;
import io.github.freya022.botcommands.api.core.reflect.ParameterType;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Base class for <b>annotated</b> text commands.
 *
 * <p>You are not required to use this if you use {@link TextCommandsDeclaration}
 *
 * @see JDATextCommandVariation @JDATextCommandVariation
 */
public abstract class TextCommand {
	/**
	 * <p>Returns a detailed embed of what the command is, it is used by the internal {@code help} command</p>
	 * <p>The {@code help} command will automatically set the embed title to be {@code Command '[command_name]'} but can be overridden</p>
	 * <p>It will also set the embed's description to be the command's description, <b>you can override with {@link EmbedBuilder#setDescription(CharSequence)}</b></p>
	 *
	 * @return The EmbedBuilder to use as a detailed description
	 *
	 * @see TextCommandBuilder#setDetailedDescription(Consumer) DSL equivalent
	 */
	@Nullable
	public Consumer<EmbedBuilder> getDetailedDescription() {return null;}

	/**
	 * Returns the generated value supplier of an {@link GeneratedOption @GeneratedOption},
	 * if the method doesn't return a generated value supplier, the framework will throw.
	 * <br>This method is called only if your option is annotated with {@link GeneratedOption @GeneratedOption}
	 *
	 * <p>This method will only be called once per command option per guild
	 *
	 * @param commandPath   The path of the command, as set in {@link JDATextCommandVariation}
	 * @param optionName    The name of the <b>transformed</b> command option, might not be equal to the parameter name
	 * @param parameterType The <b>boxed</b> type of the command option
	 *
	 * @return A {@link ApplicationGeneratedValueSupplier} to generate the option on command execution
	 */
	@NotNull
	public TextGeneratedValueSupplier getGeneratedValueSupplier(@NotNull CommandPath commandPath,
	                                                            @NotNull String optionName,
	                                                            @NotNull ParameterType parameterType) {
		throw new IllegalArgumentException("Option '%s' in command path '%s' is a generated option but no generated value supplier has been given".formatted(optionName, commandPath.getFullPath()));
	}
}
