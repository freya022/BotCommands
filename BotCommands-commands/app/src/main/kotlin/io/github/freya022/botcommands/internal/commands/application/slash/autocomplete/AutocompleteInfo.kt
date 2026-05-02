package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolder
import kotlin.reflect.KFunction

internal abstract class AutocompleteInfo internal constructor() : IDeclarationSiteHolder {
    abstract val name: String?
    abstract val function: KFunction<*>
    abstract val mode: AutocompleteMode
    abstract val showUserInput: Boolean
    abstract val autocompleteCache: AutocompleteCacheInfo?

    abstract fun invalidate()
}
