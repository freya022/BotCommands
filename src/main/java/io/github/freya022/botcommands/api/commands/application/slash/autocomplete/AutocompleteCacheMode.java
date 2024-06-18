package io.github.freya022.botcommands.api.commands.application.slash.autocomplete;

/**
 * See values
 */
public enum AutocompleteCacheMode {
    /**
     * The autocomplete choice list will be computed for each <b>new</b> key
     * <br><b>The value is assumed to always be the same at any point in time, for the same key</b>, as {@code f(key, t) = value(key)}
     * <br>The values may be computed if the key has been evicted
     */
    CONSTANT_BY_KEY
}
