package com.freya02.botcommands.internal.commands.application.slash.autocomplete.caches

import com.freya02.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

internal object NoCacheAutocomplete : AbstractAutocompleteCache() {
    override suspend fun retrieveAndCall(
        handler: AutocompleteHandler,
        event: CommandAutoCompleteInteractionEvent,
        valueComputer: suspend (CommandAutoCompleteInteractionEvent) -> List<Command.Choice>
    ): List<Command.Choice> {
        return valueComputer(event) //Always compute the value, the result gets replied by the computer
    }

    override fun invalidate() {
        //No cache to invalidate
    }
}