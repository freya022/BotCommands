package com.freya02.botcommands.api.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.caches.AbstractAutocompleteCache
import com.freya02.botcommands.internal.requireUser
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

class AutocompleteInfo internal constructor(builder: AutocompleteInfoBuilder) {
    val name: String = builder.name
    val function: KFunction<Collection<*>> = builder.function
    val mode: AutocompleteMode = builder.mode
    val showUserInput: Boolean = builder.showUserInput
    val autocompleteCache: AutocompleteCacheInfo? = builder.autocompleteCache

    @get:JvmSynthetic
    internal val cache = AbstractAutocompleteCache.fromMode(this)

    init {
        requireUser(function.valueParameters.firstOrNull()?.type?.jvmErasure == CommandAutoCompleteInteractionEvent::class, function) {
            "First parameter must be a CommandAutoCompleteInteractionEvent"
        }
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