package com.freya02.botcommands.internal.application.slash.autocomplete.caches

import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

internal object NoCacheAutocomplete : AbstractAutocompleteCache() {
    override suspend fun retrieveAndCall(
        event: CommandAutoCompleteInteractionEvent,
        valueComputer: suspend (CompositeAutocompletionKey?) -> List<Command.Choice>
    ): List<Command.Choice> {
        return valueComputer(null) //Always compute the value, the result gets replied by the computer
    }

    override fun put(key: CompositeAutocompletionKey, choices: List<Command.Choice>) {
        //Don't cache
    }

    override fun invalidate() {
        //No cache to invalidate
    }
}