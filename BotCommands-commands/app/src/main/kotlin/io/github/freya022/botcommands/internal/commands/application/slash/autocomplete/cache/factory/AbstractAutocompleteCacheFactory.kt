package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.factory

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.factory.AutocompleteCacheFactory
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.AbstractAutocompleteCache

internal abstract class AbstractAutocompleteCacheFactory : AutocompleteCacheFactory {
    internal abstract val force: Boolean

    internal abstract fun create(): AbstractAutocompleteCache
}
