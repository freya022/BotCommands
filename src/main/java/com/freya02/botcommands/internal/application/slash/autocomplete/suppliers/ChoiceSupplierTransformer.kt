package com.freya02.botcommands.internal.application.slash.autocomplete.suppliers

import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo
import com.freya02.botcommands.internal.application.slash.autocomplete.ChoiceSupplier
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

class ChoiceSupplierTransformer(
    private val handlerInfo: AutocompletionHandlerInfo,
    private val transformer: AutocompletionTransformer<Any>
) : ChoiceSupplier {
    @Throws(Exception::class)
    override fun apply(event: CommandAutoCompleteInteractionEvent, collection: Collection<*>): List<Command.Choice> {
        return collection
            .take(handlerInfo.maxChoices)
            .map { transformer.apply(it) }
    }
}