package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval

@Deprecated("Only had one mode ever, that always has been and will still be the default")
@ScheduledForRemoval
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
