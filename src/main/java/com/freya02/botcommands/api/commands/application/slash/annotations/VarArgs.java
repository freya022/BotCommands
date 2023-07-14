package com.freya02.botcommands.api.commands.application.slash.annotations;

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder;
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Generates N command options from the specified {@link SlashOption} or {@link TextOption}.
 * <br>The target parameter must be of type {@link List}.
 * <br>You can configure how many arguments are required with {@link #numRequired()}.
 * <br><b>Note:</b> you are limited to 1 vararg parameter in text commands.
 *
 * @see SlashCommandBuilder#optionVararg(String, int, int, Function1, Function2) DSL equivalent (List of options)
 * @see SlashCommandBuilder#inlineClassOptionVararg(String, Class, int, int, Function1, Function2) DSL equivalent (Aggregate containing list of options)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface VarArgs {
	/**
	 * @return The number of times this option needs to appear, which must be between 1 and {@value CommandData#MAX_OPTIONS}.
	 */
	int value();

	/**
	 * @return The number of required options for this vararg.
	 */
	int numRequired() default 1;
}
