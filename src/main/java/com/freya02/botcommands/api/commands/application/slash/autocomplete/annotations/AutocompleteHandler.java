package com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations;

import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer;
import com.freya02.botcommands.api.core.annotations.Handler;
import com.freya02.botcommands.api.core.config.BApplicationConfig;
import kotlin.reflect.KClass;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Annotation to mark methods as being autocomplete functions for {@link AppOption slash command options}.
 *
 * <br>Requirements:
 * <ul>
 *     <li>The method must be public</li>
 *     <li>The method must be non-static</li>
 *     <li>The first parameter must be {@link CommandAutoCompleteInteractionEvent}</li>
 * </ul>
 *
 * The annotated method returns a {@link List} of things
 * <br>These things can be, and are mapped as follows:
 * <ul>
 *     <li>String, Long, Double -> Choice(String, String), uses fuzzy matching to give the best choices first</li>
 *     <li>Choice -> keep the same choice, same order as provided</li>
 *     <li>Object -> Transformer -> Choice, same order as provided</li>
 * </ul>
 * <b>Note that the first choice is always what the user typed</b>
 *
 * <p>
 *
 * You can add more List element types with {@link BApplicationConfig#registerAutocompleteTransformer(KClass, AutocompleteTransformer)}
 *
 * <h2>State aware autocomplete</h2>
 * You can also use "state aware autocomplete", this means you can retrieve parameters the user has already entered and use it to make your autocomplete better
 *
 * <br>The requirements are as follows:
 * <ul>
 *     <li>The parameters must be annotated with {@link AppOption} if they are on the original slash commands too</li>
 *     <li>The parameters must be named the same as in the original slash command</li>
 *     <li>The parameters of the same name must have the same type as the original slash command</li>
 * </ul>
 *
 * However:
 * <ul>
 *     <li>The parameters can be in any order</li>
 *     <li>You are free to put as many or less parameters as the original slash commands</li>
 *     <li>You can also use custom parameters, like getting a JDA instance, these do not have to be on the original slash commands</li>
 * </ul>
 *
 * <p><b>Requirement:</b> The declaring class must be annotated with {@link Handler}.
 *
 * @see AppOption
 * @see JDASlashCommand
 * @see CompositeKey
 * @see CacheAutocomplete
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
	 * @see AutocompleteMode
	 */
	AutocompleteMode mode() default AutocompleteMode.FUZZY;

	/**
	 * Determines if the user input is shown as the first suggestion
	 * <br>This allows the user to force his own input more easily
	 * <br><b>This being <code>false</code> does not mean that the bot-provided choices are forced upon the user, autocomplete is never forced, unlike choices</b>
	 *
	 * @return <code>true</code> if the user's input should be shown, <code>false</code> if not
	 */
	boolean showUserInput() default true;
}
