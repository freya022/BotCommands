package com.freya02.botcommands.api.commands.application.slash.autocomplete.builder

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.internal.BContextImpl
import kotlin.reflect.KFunction

class AutocompleteInfoBuilder internal constructor(private val context: BContextImpl, val name: String, override val function: KFunction<Collection<Any>>) : IBuilderFunctionHolder<Collection<*>> {
    /**
     * **Annotation equivalent:** [AutocompleteHandler.mode]
     *
     * @see AutocompleteHandler.mode
     */
    var mode: AutocompleteMode = AutocompleteMode.FUZZY

    /**
     * **Annotation equivalent:** [AutocompleteHandler.showUserInput]
     *
     * @see AutocompleteHandler.showUserInput
     */
    var showUserInput: Boolean = true

    internal var autocompleteCache: AutocompleteCacheInfo? = null
        private set

    /**
     * **Annotation equivalent:** [CacheAutocomplete]
     *
     * @see CacheAutocomplete
     */
    fun cache(cacheMode: AutocompleteCacheMode, block: AutocompleteCacheInfoBuilder.() -> Unit = {}) {
        autocompleteCache = AutocompleteCacheInfoBuilder(cacheMode).apply(block).build()
    }

    internal fun build(): AutocompleteInfo {
        return AutocompleteInfo(context, this)
    }
}
