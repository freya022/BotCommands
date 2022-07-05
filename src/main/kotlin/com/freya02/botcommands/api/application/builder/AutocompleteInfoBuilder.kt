package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.AutocompleteCacheInfo
import com.freya02.botcommands.api.application.AutocompleteInfo
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode
import com.freya02.botcommands.api.builder.BuilderFunctionHolder
import com.freya02.botcommands.internal.throwUser

class AutocompleteInfoBuilder internal constructor() : BuilderFunctionHolder<Collection<*>>() {
    var mode: AutocompletionMode = AutocompletionMode.FUZZY
    var showUserInput: Boolean = false
    var autocompleteCache: AutocompleteCacheInfo? = null
        private set

    fun cache(block: AutocompleteCacheInfoBuilder.() -> Unit) {
        autocompleteCache = AutocompleteCacheInfoBuilder().apply(block).build()
    }

    internal fun build(): AutocompleteInfo {
        if (!isFunctionInitialized()) {
            throwUser("An autocompleted option must have its function set")
        }

        return AutocompleteInfo(this)
    }
}