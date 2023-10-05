package com.freya02.botcommands.api.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.caches.AbstractAutocompleteCache
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.caches.NoCacheAutocomplete
import com.freya02.botcommands.internal.core.BContextImpl
import com.freya02.botcommands.internal.core.reflection.toMemberEventFunction
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

class AutocompleteInfo internal constructor(context: BContextImpl, builder: AutocompleteInfoBuilder) {
    val name: String = builder.name
    val eventFunction = builder.function.toMemberEventFunction<CommandAutoCompleteInteractionEvent, _>(context)
    val mode: AutocompleteMode = builder.mode
    val showUserInput: Boolean = builder.showUserInput
    val autocompleteCache: AutocompleteCacheInfo? = builder.autocompleteCache

    val function get() = eventFunction.kFunction

    @get:JvmSynthetic
    internal val cache = when {
        context.config.disableAutocompleteCache && builder.autocompleteCache?.force != true -> NoCacheAutocomplete
        else -> AbstractAutocompleteCache.fromMode(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AutocompleteInfo

        if (name != other.name) return false
        if (function != other.function) return false
        if (mode != other.mode) return false
        if (showUserInput != other.showUserInput) return false
        if (autocompleteCache != other.autocompleteCache) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + function.hashCode()
        result = 31 * result + mode.hashCode()
        result = 31 * result + showUserInput.hashCode()
        result = 31 * result + (autocompleteCache?.hashCode() ?: 0)
        return result
    }
}
