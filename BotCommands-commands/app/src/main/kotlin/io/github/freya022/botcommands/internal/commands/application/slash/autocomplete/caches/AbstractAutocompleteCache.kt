package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.caches

import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteCacheInfo
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

internal sealed class AbstractAutocompleteCache {
    abstract val compositeKeys: Set<String>

    abstract suspend fun retrieveAndCall(
        handler: AutocompleteHandler,
        event: CommandAutoCompleteInteractionEvent,
        valueComputer: suspend (CommandAutoCompleteInteractionEvent) -> List<Command.Choice>
    ): List<Command.Choice>

    abstract fun invalidate()

    companion object {
        //In case more caches are to come
        fun fromMode(autocompleteCache: AutocompleteCacheInfo): AbstractAutocompleteCache {
            return ConstantByKeyAutocompleteCache(autocompleteCache)
        }
    }
}
