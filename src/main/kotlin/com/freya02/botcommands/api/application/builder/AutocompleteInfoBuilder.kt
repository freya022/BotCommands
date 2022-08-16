package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.AutocompleteCacheInfo
import com.freya02.botcommands.api.application.AutocompleteInfo
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteMode
import com.freya02.botcommands.api.builder.BuilderFunctionHolder

class AutocompleteInfoBuilder internal constructor() : BuilderFunctionHolder<Collection<*>>() {
    var mode: AutocompleteMode = AutocompleteMode.FUZZY
    var showUserInput: Boolean = false
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