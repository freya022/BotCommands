package com.freya02.botcommands.internal.commands.application.slash.autocomplete.suppliers

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

internal sealed interface ChoiceSupplier {
    fun apply(event: CommandAutoCompleteInteractionEvent, collection: Collection<Any>): List<Command.Choice>
}