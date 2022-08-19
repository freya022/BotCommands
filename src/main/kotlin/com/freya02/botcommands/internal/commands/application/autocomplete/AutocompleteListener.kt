package com.freya02.botcommands.internal.commands.application.autocomplete

import com.freya02.botcommands.api.commands.application.CommandPath
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

@BService
class AutocompleteListener(private val context: BContextImpl) {
    @BEventListener
    internal suspend fun onAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val slashCommand = CommandPath.of(event.commandPath).let {
            context.applicationCommandsContext.findLiveSlashCommand(event.guild, it)
                ?: context.applicationCommandsContext.findLiveSlashCommand(null, it)
                ?: throwUser("A slash command could not be found: ${event.commandPath}")
        }

        for (optionParameter in slashCommand.optionParameters) {
            if (optionParameter.discordName == event.focusedOption.name) {
                val autocompleteHandler = optionParameter.autocompleteHandler ?:
                    throwUser("Autocomplete handler was not found")

                event.replyChoices(autocompleteHandler.handle(event)).queue()

                break
            }
        }
    }
}