package com.freya02.botcommands.internal.application.slash.autocomplete.caches

import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionCacheMode
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice

internal sealed class AbstractAutocompleteCache {
    abstract suspend fun retrieveAndCall(
        event: CommandAutoCompleteInteractionEvent,
        valueComputer: suspend (CompositeAutocompletionKey?) -> List<Choice>
    ): List<Choice>

    abstract fun put(key: CompositeAutocompletionKey, choices: List<Choice>)
    abstract fun invalidate()

    companion object {
        //In case more caches are to come
        fun fromMode(handler: AutocompleteHandler): AbstractAutocompleteCache {
            return when (handler.autocompleteInfo.cacheMode) {
                AutocompletionCacheMode.NO_CACHE -> NoCacheAutocomplete
                AutocompletionCacheMode.CONSTANT_BY_KEY -> ConstantByKeyAutocompleteCache(handler)
            }
        }
    }
}