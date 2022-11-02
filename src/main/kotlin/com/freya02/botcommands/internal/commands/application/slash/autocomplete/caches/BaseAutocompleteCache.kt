package com.freya02.botcommands.internal.commands.application.slash.autocomplete.caches

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheInfo
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.CompositeAutocompleteKey
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

private typealias EntityCacheFunction = (CommandAutoCompleteInteractionEvent) -> Long

internal sealed class BaseAutocompleteCache(cacheInfo: AutocompleteCacheInfo) : AbstractAutocompleteCache() {
    private val guildFunction: EntityCacheFunction =
        getEntityCacheFunction(cacheInfo.guildLocal) { if (it.guild != null) it.guild!!.idLong else 0 }
    private val channelFunction: EntityCacheFunction =
        getEntityCacheFunction(cacheInfo.channelLocal) { if (it.channel != null) it.channel!!.idLong else 0 }
    private val userFunction: EntityCacheFunction = getEntityCacheFunction(cacheInfo.userLocal) { it.user.idLong }

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

        autocompleteHandler.compositeParameters.forEach { parameter ->
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
    ): CompositeAutocompleteKey {
        val compositeOptionValues: Array<String> = getCompositeOptionValues(handler, event)
        return CompositeAutocompleteKey(
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