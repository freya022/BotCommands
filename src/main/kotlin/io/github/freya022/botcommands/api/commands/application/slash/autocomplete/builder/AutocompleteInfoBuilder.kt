@file:Suppress("DEPRECATION")

package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolderBuilder

interface AutocompleteInfoBuilder : IDeclarationSiteHolderBuilder {
    /**
     * Sets the [autocomplete mode][AutocompleteMode].
     *
     * **This is only usable on collection return types of String, Double and Long**
     *
     * @see AutocompleteHandler.mode
     */
    var mode: AutocompleteMode

    /**
     * Whether the user input is shown as the first suggestion.
     *
     * **Note:** `false` does not mean that the bot-provided choices are forced upon the user,
     * autocomplete is never forced, unlike choices.
     *
     * **Default:** `false`
     *
     * @see AutocompleteHandler.showUserInput
     */
    var showUserInput: Boolean

    /**
     * Sets up autocomplete caching.
     *
     * The cache key is the input of the focused option, however,
     * you can use composite keys if you want to cache based off multiple option values,
     * see [AutocompleteCacheInfoBuilder.compositeKeys] for more details.
     *
     * @see CacheAutocomplete @CacheAutocomplete
     */
    fun cache(block: AutocompleteCacheInfoBuilder.() -> Unit = {})
}

