package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.suppliers

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteAlgorithms
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler.Companion.asChoice
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

internal class ChoiceSupplierStringFuzzy(private val numChoices: Int) : ChoiceSupplier {
    @Throws(Exception::class)
    override fun apply(event: CommandAutoCompleteInteractionEvent, collection: Collection<Any>): List<Command.Choice> {
        val autoCompleteQuery = event.focusedOption
        return AutocompleteAlgorithms.fuzzyMatching(collection, { it.toString() }, event.focusedOption.value)
            .take(numChoices)
            .map { it.string.asChoice(autoCompleteQuery.type) ?: throwArgument("Malformed input for option type ${autoCompleteQuery.type}: '${it.string}'") }
    }
}