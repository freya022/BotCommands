package com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations;

import com.freya02.botcommands.annotations.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionCacheMode;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to cache autocompletion handlers results
 * <br>By default this will cache results by key
 * <br>By default, that key is solely the input of the focused option
 * <br>However you can use composite keys if your input depends on more than the focused option, you will have to use {@link CompositeKey} on the values to be included in the key
 *
 * @see AppOption
 * @see AutocompletionHandler
 * @see CompositeKey
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CacheAutocompletion {
	/**
	 * Sets the {@link AutocompletionCacheMode autocompletion cache mode}
	 * <br>You can mark app options your autocompletion depends on as composite keys, this would be useful to make an autocompletion result depend on multiple options, instead of only the focused one
	 *
	 * @return Mode of the autocompletion cache
	 * @see CompositeKey
	 */
	AutocompletionCacheMode cacheMode() default AutocompletionCacheMode.CONSTANT_BY_KEY;

	/**
	 * Sets the cache size for this autocompletion cache, <b>in kilobytes (KB)</b>
	 * <br>This will work only on {@link AutocompletionCacheMode#CONSTANT_BY_KEY}
	 *
	 * @return The cache size for this autocompletion mode
	 */
	long cacheSize() default 2048;

	/**
	 * Defines whether this autocompletion will give different results based on which {@link Guild} this interaction is executing on
	 *
	 * @return <code>true</code> if the autocompletion depends on the {@link Guild} this interaction is execution on
	 */
	boolean guildLocal() default false;

	/**
	 * Defines whether this autocompletion will give different results based on which {@link User} is executing this interaction
	 *
	 * @return <code>true</code> if the autocompletion depends on which {@link User} is executing this interaction
	 */
	boolean userLocal() default false;

	/**
	 * Defines whether this autocompletion will give different results based on which {@link Channel} this interaction is executing on
	 *
	 * @return <code>true</code> if the autocompletion depends on the {@link Channel} this interaction is execution on
	 */
	boolean channelLocal() default false;
}
