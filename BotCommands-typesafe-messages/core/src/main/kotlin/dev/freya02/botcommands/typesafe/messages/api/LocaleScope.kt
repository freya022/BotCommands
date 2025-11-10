package dev.freya02.botcommands.typesafe.messages.api

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi

/**
 * Enumeration of locale preferences.
 */
@ExperimentalTypesafeMessagesApi
enum class LocaleScope {

    /**
     * Prefers using the user locale, falls back to [GUILD] if unavailable.
     */
    PREFER_USER,

    /**
     * Uses the guild locale.
     */
    GUILD,
}
