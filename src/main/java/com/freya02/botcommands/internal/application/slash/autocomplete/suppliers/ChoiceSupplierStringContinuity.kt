package com.freya02.botcommands.internal.application.slash.autocomplete.suppliers

import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteAlgorithms
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo.Companion.getChoice
import com.freya02.botcommands.internal.application.slash.autocomplete.ChoiceSupplier
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

class ChoiceSupplierStringContinuity(private val handlerInfo: AutocompletionHandlerInfo) : ChoiceSupplier {
    @Throws(Exception::class)
    override fun apply(event: CommandAutoCompleteInteractionEvent, collection: Collection<*>): List<Command.Choice> {
        val autoCompleteQuery = event.focusedOption
        return AutocompleteAlgorithms.fuzzyMatchingWithContinuity(collection, { obj -> obj.toString() }, event)
            .take(handlerInfo.maxChoices)
            .map { getChoice(autoCompleteQuery.type, it.string) ?: throw IllegalArgumentException("Malformed input for option type ${autoCompleteQuery.type}: '${it.string}'") }
    }
}