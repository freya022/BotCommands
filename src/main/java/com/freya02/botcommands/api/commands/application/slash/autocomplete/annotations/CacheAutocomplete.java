package com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations;

import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode;
import com.freya02.botcommands.api.core.config.BConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to cache autocomplete handlers results
 * <br>By default this will cache results by key
 * <br>By default, that key is solely the input of the focused option
 * <br>However you can use composite keys if your input depends on more than the focused option, you will have to use {@link CompositeKey} on the values to be included in the key
 *
 * @see AppOption
 * @see AutocompleteHandler
 * @see CompositeKey
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CacheAutocomplete {
	/**
	 * Sets the {@link AutocompleteCacheMode autocomplete cache mode}
	 * <br>You can mark app options your autocomplete depends on as composite keys, this would be useful to make an autocomplete result depend on multiple options, instead of only the focused one
	 *
	 * @return Mode of the autocomplete cache
	 * @see CompositeKey
	 */
	AutocompleteCacheMode cacheMode() default AutocompleteCacheMode.CONSTANT_BY_KEY;

	/**
	 * Whether the cache should be used even if {@link BConfig#setDisableAutocompleteCache(boolean) autocomplete cache is disabled}.
	 * <br>This could be useful if your autocomplete is heavy even in a development environment.
	 *
	 * @return {@code} true if the autocomplete results should be cached anyway
	 *
	 * @see BConfig#setDisableAutocompleteCache(boolean)
	 */
	boolean forceCache() default false;

	/**
	 * Sets the cache size for this autocomplete cache, <b>in kilobytes (KB)</b>
	 * <br>This will work only on {@link AutocompleteCacheMode#CONSTANT_BY_KEY}
	 *
	 * @return The cache size for this autocomplete mode
	 */
	long cacheSize() default 2048;

	/**
	 * Defines whether this autocomplete will give different results based on which {@link Guild} this interaction is executing on
	 *
	 * @return <code>true</code> if the autocomplete depends on the {@link Guild} this interaction is execution on
	 */
	boolean guildLocal() default false;

	/**
	 * Defines whether this autocomplete will give different results based on which {@link User} is executing this interaction
	 *
	 * @return <code>true</code> if the autocomplete depends on which {@link User} is executing this interaction
	 */
	boolean userLocal() default false;

	/**
	 * Defines whether this autocomplete will give different results based on which {@link Channel} this interaction is executing on
	 *
	 * @return <code>true</code> if the autocomplete depends on the {@link Channel} this interaction is execution on
	 */
	boolean channelLocal() default false;
}
