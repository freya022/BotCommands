package com.freya02.botcommands.api.application.slash.autocomplete;

/**
 * See values
 */
public enum AutocompletionCacheMode {
	/**
	 * The autocompletion choice list will be computed for each <b>new</b> key
	 * <br><b>The value is assumed to always be the same at any point in time, for the same key</b>, as <code>f(key, t) = value(key)</code>
	 * <br>The values may be computed if the key has been evicted
	 */
	CONSTANT_BY_KEY,
	/**
	 * The autocompletion choice list will always be computed
	 */
	NO_CACHE
}
