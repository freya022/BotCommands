package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
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

    /**
     * Sets up autocomplete caching.
     *
     * By default, this will cache results by key, which is the input of the focused option.<br>
     * However, you can use composite keys if you want to cache based off multiple option values,
     * see [AutocompleteCacheInfoBuilder.compositeKeys] for more details.
     *
     * @see CacheAutocomplete @CacheAutocomplete
     */
    @Deprecated("Only had one mode ever, that always has been and will still be the default", ReplaceWith("cache(block)"))
    fun cache(cacheMode: AutocompleteCacheMode, block: AutocompleteCacheInfoBuilder.() -> Unit = {}) = cache(block)
}

