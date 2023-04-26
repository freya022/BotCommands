package com.freya02.botcommands.api.commands.application.slash.autocomplete.builder

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import com.freya02.botcommands.api.commands.builder.BuilderFunctionHolder
import com.freya02.botcommands.internal.BContextImpl

class AutocompleteInfoBuilder internal constructor(private val context: BContextImpl, val name: String) : BuilderFunctionHolder<Collection<*>>() {
    var mode: AutocompleteMode = AutocompleteMode.FUZZY
    var showUserInput: Boolean = true
    var autocompleteCache: AutocompleteCacheInfo? = null
        private set

    fun cache(cacheMode: AutocompleteCacheMode, block: AutocompleteCacheInfoBuilder.() -> Unit = {}) {
        autocompleteCache = AutocompleteCacheInfoBuilder(cacheMode).apply(block).build()
    }

    internal fun build(): AutocompleteInfo {
        return AutocompleteInfo(context, this)
    }
}
