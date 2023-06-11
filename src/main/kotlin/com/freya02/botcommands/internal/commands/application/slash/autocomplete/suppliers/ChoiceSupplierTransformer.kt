package com.freya02.botcommands.internal.commands.application.slash.autocomplete.suppliers

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

internal class ChoiceSupplierTransformer(
    private val transformer: AutocompleteTransformer<Any>,
    private val numChoices: Int
) : ChoiceSupplier {
    @Throws(Exception::class)
    override fun apply(event: CommandAutoCompleteInteractionEvent, collection: Collection<Any>): List<Command.Choice> {
        return collection
            .take(numChoices)
            .map { transformer.apply(it) }
    }
}