package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheInfo
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.internal.core.BContextImpl
import kotlin.reflect.KFunction

class AutocompleteInfoBuilder internal constructor(private val context: BContextImpl, val name: String, override val function: KFunction<Collection<Any>>) : IBuilderFunctionHolder<Collection<*>> {
    /**
     * Sets the [autocomplete mode][AutocompleteMode].
     *
     * **This is only usable on collection return types of String, Double and Long**
     *
     * @see AutocompleteHandler.mode
     */
    var mode: AutocompleteMode = AutocompleteMode.FUZZY

    /**
     * Determines if the user input is shown as the first suggestion
     *
     * This allows the user to force his own input more easily
     *
     * **This being `false` does not mean that the bot-provided choices are forced upon the user,
     * autocomplete is never forced, unlike choices**
     *
     * @return `true` if the user's input should be shown, `false` if not
     *
     * @see AutocompleteHandler.showUserInput
     */
    var showUserInput: Boolean = true

    internal var autocompleteCache: AutocompleteCacheInfo? = null
        private set

    /**
     * Sets up autocomplete caching.
     *
     * By default, this will cache results by key, which is the input of the focused option.<br>
     * However, you can use composite keys if you want to cache based off multiple option values,
     * see [AutocompleteCacheInfoBuilder.compositeKeys] for more details.
     *
     * @see CacheAutocomplete @CacheAutocomplete
     */
    fun cache(cacheMode: AutocompleteCacheMode, block: AutocompleteCacheInfoBuilder.() -> Unit = {}) {
        autocompleteCache = AutocompleteCacheInfoBuilder(cacheMode).apply(block).build()
    }

    internal fun build(): AutocompleteInfo {
        return AutocompleteInfo(context, this)
    }
}
