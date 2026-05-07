package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache

import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

abstract class AbstractAutocompleteCache {
    abstract val compositeKeys: Set<String>

    abstract suspend fun retrieveAndCall(
        handler: AutocompleteHandler,
        event: CommandAutoCompleteInteractionEvent,
        valueComputer: suspend (CommandAutoCompleteInteractionEvent) -> List<Command.Choice>
    ): List<Command.Choice>

    abstract fun invalidate()
}
