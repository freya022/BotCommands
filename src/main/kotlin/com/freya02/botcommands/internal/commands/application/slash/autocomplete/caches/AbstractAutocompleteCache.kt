package com.freya02.botcommands.internal.commands.application.slash.autocomplete.caches

import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

internal sealed class AbstractAutocompleteCache {
    abstract suspend fun retrieveAndCall(
        event: CommandAutoCompleteInteractionEvent,
        valueComputer: suspend (CommandAutoCompleteInteractionEvent) -> List<Command.Choice>
    ): List<Command.Choice>

    abstract fun invalidate()

    companion object {
        //In case more caches are to come
        fun fromMode(handler: AutocompleteHandler): AbstractAutocompleteCache {
            val autocompleteCache = handler.autocompleteInfo.autocompleteCache ?: return NoCacheAutocomplete

            return when (autocompleteCache.cacheMode) {
                AutocompleteCacheMode.NO_CACHE -> NoCacheAutocomplete
                AutocompleteCacheMode.CONSTANT_BY_KEY -> ConstantByKeyAutocompleteCache(handler, autocompleteCache)
            }
        }
    }
}