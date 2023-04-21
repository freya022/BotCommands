package com.freya02.botcommands.internal.commands.application.autocomplete

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.toVarArgName
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.math.max

@BService
internal class AutocompleteListener(private val context: BContextImpl) {
    @BEventListener
    internal suspend fun onAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val slashCommand = CommandPath.of(event.fullCommandName).let {
            context.applicationCommandsContext.findLiveSlashCommand(event.guild, it)
                ?: context.applicationCommandsContext.findLiveSlashCommand(null, it)
                ?: throwUser("A slash command could not be found: ${event.fullCommandName}")
        }

        for (optionParameter in slashCommand.optionParameters) {
            val arguments = max(1, optionParameter.varArgs)

            for (varArgNum in 0 until arguments) {
                val varArgName = optionParameter.discordName.toVarArgName(varArgNum)
                if (varArgName == event.focusedOption.name) {
                    val autocompleteHandler =
                        optionParameter.autocompleteHandler ?: throwUser("Autocomplete handler was not found")

                    event.replyChoices(autocompleteHandler.handle(event)).queue()

                    return
                }
            }
        }
    }
}