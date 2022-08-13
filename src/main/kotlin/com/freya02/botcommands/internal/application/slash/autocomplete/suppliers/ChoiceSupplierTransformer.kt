package com.freya02.botcommands.internal.application.slash.autocomplete.suppliers

import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteTransformer
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

internal class ChoiceSupplierTransformer(
    private val transformer: AutocompleteTransformer<Any>,
    private val numChoices: Int
) : ChoiceSupplier {
    @Throws(Exception::class)
    override fun apply(event: CommandAutoCompleteInteractionEvent, collection: Collection<*>): List<Command.Choice> {
        return collection
            .take(numChoices)
            .map { transformer.apply(it) }
    }
}