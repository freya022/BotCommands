package com.freya02.botcommands.internal.application.slash.autocomplete.caches

import com.freya02.botcommands.api.application.AutocompleteInfo
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompleteCommandParameter
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey
import com.freya02.botcommands.internal.parameters.MethodParameterType
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

private typealias EntityCacheFunction = (CommandAutoCompleteInteractionEvent) -> Long

internal sealed class BaseAutocompleteCache(info: AutocompleteInfo) : AbstractAutocompleteCache() {
    private val guildFunction: EntityCacheFunction =
        getEntityCacheFunction(info.guildLocal) { if (it.guild != null) it.guild!!.idLong else 0 }
    private val channelFunction: EntityCacheFunction =
        getEntityCacheFunction(info.channelLocal) { if (it.channel != null) it.channel!!.idLong else 0 }
    private val userFunction: EntityCacheFunction = getEntityCacheFunction(info.userLocal) { it.user.idLong }

    private fun getCompositeOptionValues(
        autocompleteHandler: AutocompleteHandler,
        event: CommandAutoCompleteInteractionEvent
    ): Array<String> {
        val optionValues: MutableList<String> = ArrayList()

        //Identify the cached value by its command path too !
        optionValues.add(event.name)
        event.subcommandGroup?.let { optionValues.add(it) }
        event.subcommandName?.let { optionValues.add(it) }

        optionValues.add(event.focusedOption.value)

        autocompleteHandler
            .methodParameters
            .asSequence()
            .filter { it.methodParameterType == MethodParameterType.COMMAND }
            .map { it as AutocompleteCommandParameter }
            .filter { it.isCompositeKey }
            .forEach { parameter ->
                val optionName = parameter.discordName
                val option = event.getOption(optionName)

                when {
                    option == null -> optionValues.add("null")
                    event.focusedOption.name != optionName -> optionValues.add(option.asString)
                }
            }

        return optionValues.toTypedArray()
    }

    protected fun getCompositeKey(
        handler: AutocompleteHandler,
        event: CommandAutoCompleteInteractionEvent
    ): CompositeAutocompletionKey {
        val compositeOptionValues: Array<String> = getCompositeOptionValues(handler, event)
        return CompositeAutocompletionKey(
            compositeOptionValues,
            guildFunction(event),
            channelFunction(event),
            userFunction(event)
        )
    }

    companion object {
        private fun getEntityCacheFunction(flag: Boolean, func: EntityCacheFunction): EntityCacheFunction = when {
            flag -> func
            else -> { _ -> 0 }
        }
    }
}