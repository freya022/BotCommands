package com.freya02.botcommands.api.application.slash.autocomplete.annotations;

import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionCacheMode;

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
}
