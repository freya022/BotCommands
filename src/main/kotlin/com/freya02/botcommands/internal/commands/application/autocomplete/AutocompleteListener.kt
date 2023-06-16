package com.freya02.botcommands.internal.commands.application.autocomplete

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandOption
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils.function
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

@BService
internal class AutocompleteListener(private val context: BContextImpl) {
    @BEventListener
    internal suspend fun onAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val slashCommand = CommandPath.of(event.fullCommandName).let {
            context.applicationCommandsContext.findLiveSlashCommand(event.guild, it)
                ?: throwUser("A slash command could not be found: ${event.fullCommandName}")
        }

        for (option in slashCommand.parameters.flatMap { it.allOptions }) {
            if (option.optionType != OptionType.OPTION) continue
            option as SlashCommandOption

            if (option.discordName == event.focusedOption.name) {
                val autocompleteHandler = option.autocompleteHandler
                    ?: throwUser(option.kParameter.function, "Autocomplete handler was not found on parameter '${option.declaredName}'")

                return event.replyChoices(autocompleteHandler.handle(event)).queue()
            }
        }
    }
}