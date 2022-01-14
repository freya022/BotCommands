package com.freya02.botcommands.api.application.slash.annotations;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionCacheMode;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Annotation to mark methods as being autocompletion functions for {@link AppOption slash command options}
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
 * You can add more List element types with {@link CommandsBuilder#registerAutocompletionTransformer(Class, AutocompletionTransformer)}
 *
 * <p>
 *
 * <h2>State aware autocompletion</h2>
 * You can also use "state aware autocompletion", this means you can retrieve parameters the user has already entered and use it to make your autocompletion better
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
 * @see AppOption
 * @see JDASlashCommand
 * @see CompositeKey
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AutocompletionHandler {
	/**
	 * Sets the name of the autocompletion handler, <b>it must be the same as what you set in {@link AppOption#autocomplete()}</b>
	 * <br>The name must be unique, another handler cannot share it
	 *
	 * @return Name of the autocompletion handler
	 */
	String name();

	/**
	 * Sets the {@link AutocompletionMode autocompletion mode}
	 * <br><b>This is only usable on collection return types of String, Double and Long</b>
	 *
	 * @return Mode of the autocompletion
	 * @see AutocompletionMode
	 */
	AutocompletionMode mode() default AutocompletionMode.FUZZY;

	/**
	 * Sets the {@link AutocompletionCacheMode autocompletion cache mode}
	 * <br>You can mark app options your autocompletion depends on as composite keys, this would be useful to make an autocompletion result depend on multiple options, instead of only the focused one
	 *
	 * @return Mode of the autocompletion cache
	 * @see CompositeKey
	 */
	AutocompletionCacheMode cacheMode() default AutocompletionCacheMode.NO_CACHE;

	/**
	 * Sets the cache size for this autocompletion cache, <b>in kilobytes (KB)</b>
	 * <br>This will work only on {@link AutocompletionCacheMode#CONSTANT_BY_KEY}
	 *
	 * @return The cache size for this autocompletion mode
	 */
	long cacheSize() default 2048;

	/**
	 * Determines if the user input is shown as the first suggestion
	 * <br>This allows the user to force his own input more easily
	 * <br><b>This being <code>false</code> does not mean that the bot-provided choices are forced upon the user, autocompletion is never forced, unlike choices</b>
	 *
	 * @return <code>true</code> if the user's input should be shown, <code>false</code> if not
	 */
	boolean showUserInput() default true;
}
