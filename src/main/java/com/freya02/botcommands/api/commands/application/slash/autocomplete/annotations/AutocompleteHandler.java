package com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations;

import com.freya02.botcommands.api.commands.annotations.Command;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder;
import com.freya02.botcommands.api.core.annotations.Handler;
import com.freya02.botcommands.api.core.config.BApplicationConfigBuilder;
import kotlin.jvm.functions.Function1;
import kotlin.reflect.KClass;
import kotlin.reflect.KFunction;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Uses the method as an autocomplete function for {@link AppOption slash command options}.
 *
 * <p>Requirements:
 * <ul>
 *     <li>The method must be public</li>
 *     <li>The method must be non-static</li>
 *     <li>The first parameter must be {@link CommandAutoCompleteInteractionEvent}</li>
 * </ul>
 *
 * The annotated method returns a {@link List} of the following types:
 * <ul>
 *     <li>String, Long, Double -> Choice(String, String), uses fuzzy matching to give the best choices first</li>
 *     <li>Choice -> keep the same choice, same order as provided</li>
 *     <li>T (the type of your choice) -> Transformer -> Choice, same order as provided
 *         <br>i.e. this means that an {@link AutocompleteTransformer} will be used to transform the items of your list, while preserving the order</li>
 * </ul>
 *
 * <p>
 *
 * You can add more List element types with {@link BApplicationConfigBuilder#registerAutocompleteTransformer(KClass)}
 *
 * <h2>State aware autocomplete</h2>
 * You can also use state-aware autocomplete,
 * as you can retrieve parameters the user has <b>already</b> entered.
 *
 * <br>The requirements are as follows:
 * <ul>
 *     <li>The parameters must be named the same as in the original slash command</li>
 *     <li>The parameters of the same name must have the same type as the original slash command</li>
 * </ul>
 *
 * You are free to add custom options, or omit parameters.
 *
 * <p><b>Requirement:</b> The declaring class must be annotated with {@link Handler} or be in an existing {@link Command} class.
 *
 * @see AppOption
 * @see JDASlashCommand
 * @see CacheAutocomplete
 *
 * @see SlashCommandOptionBuilder#autocomplete(String, KFunction, Function1) DSL equivalent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AutocompleteHandler {
	/**
	 * Sets the name of the autocomplete handler, <b>it must be the same as what you set in {@link AppOption#autocomplete()}</b>
	 * <br>The name must be unique, another handler cannot share it
	 *
	 * @return Name of the autocomplete handler
	 */
	String name();

	/**
	 * Sets the {@link AutocompleteMode autocomplete mode}
	 * <br><b>This is only usable on collection return types of String, Double and Long</b>
	 *
	 * @return Mode of the autocomplete
	 *
	 * @see AutocompleteMode
	 *
	 * @see AutocompleteInfoBuilder#setMode(AutocompleteMode) DSL equivalent
	 */
	AutocompleteMode mode() default AutocompleteMode.FUZZY;

	/**
	 * Determines if the user input is shown as the first suggestion
	 * <br>This allows the user to force his own input more easily
	 * <br><b>This being {@code false} does not mean that the bot-provided choices are forced upon the user, autocomplete is never forced, unlike choices</b>
	 *
	 * @return {@code true} if the user's input should be shown, {@code false} if not
	 *
	 * @see AutocompleteInfoBuilder#setShowUserInput(boolean) DSL equivalent
	 */
	boolean showUserInput() default true;
}
