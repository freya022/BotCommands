package com.freya02.botcommands.internal.application.slash.autocomplete

import com.freya02.botcommands.api.application.AutocompleteInfo
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

class AutocompleteHandler(private val slashCommandInfo: SlashCommandInfo, private val autocompleteInfo: AutocompleteInfo) {
    private val instance = slashCommandInfo.context.serviceContainer.getFunctionService(autocompleteInfo.method)

    suspend fun handle(event: CommandAutoCompleteInteractionEvent): Collection<Choice> {
        val objects: MutableMap<KParameter, Any?> = mutableMapOf()
        objects[autocompleteInfo.method.instanceParameter!!] = instance
        objects[autocompleteInfo.method.valueParameters.first()] = event

        slashCommandInfo.putSlashOptions(event, objects)

        return autocompleteInfo.method.callSuspendBy(objects) as Collection<Choice>
    }
}
