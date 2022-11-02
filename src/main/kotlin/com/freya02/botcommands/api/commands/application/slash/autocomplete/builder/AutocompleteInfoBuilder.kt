package com.freya02.botcommands.api.commands.application.slash.autocomplete.builder

import com.freya02.botcommands.api.builder.BuilderFunctionHolder
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode

class AutocompleteInfoBuilder internal constructor(val name: String) : BuilderFunctionHolder<Collection<*>>() {
    var mode: AutocompleteMode = AutocompleteMode.FUZZY
    var showUserInput: Boolean = true
    var autocompleteCache: AutocompleteCacheInfo? = null
        private set

    fun cache(block: AutocompleteCacheInfoBuilder.() -> Unit) {
        autocompleteCache = AutocompleteCacheInfoBuilder().apply(block).build()
    }

    internal fun build(): AutocompleteInfo {
        checkFunction()
        return AutocompleteInfo(this)
    }
}