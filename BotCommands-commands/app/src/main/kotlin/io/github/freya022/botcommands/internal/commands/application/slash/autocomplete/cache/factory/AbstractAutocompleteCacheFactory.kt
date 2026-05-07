package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.factory

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.factory.AutocompleteCacheFactory
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.AbstractAutocompleteCache

abstract class AbstractAutocompleteCacheFactory : AutocompleteCacheFactory {
    abstract val force: Boolean

    abstract fun create(): AbstractAutocompleteCache
}
