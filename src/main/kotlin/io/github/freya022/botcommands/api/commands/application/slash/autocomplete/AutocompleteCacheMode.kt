package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

enum class AutocompleteCacheMode {
    /**
     *
     * The autocomplete choice list will be computed for each **new** key
     *
     * **The value is assumed to always be the same at any point in time, for the same key**, as `f(key, t) = value(key)`
     *
     * The values may be computed if the key has been evicted
     */
    CONSTANT_BY_KEY
}
