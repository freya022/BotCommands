package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.caches.AbstractAutocompleteCache
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.caches.NoCacheAutocomplete
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

//See AutocompleteHandler for implementation details
class AutocompleteInfo internal constructor(context: BContext, builder: AutocompleteInfoBuilder) {
    val name: String = builder.name
    val eventFunction = builder.function.toMemberParamFunction<CommandAutoCompleteInteractionEvent, _>(context)
    val mode: AutocompleteMode = builder.mode
    val showUserInput: Boolean = builder.showUserInput
    val autocompleteCache: AutocompleteCacheInfo? = builder.autocompleteCache

    val function get() = eventFunction.kFunction

    @get:JvmSynthetic
    internal val cache = when {
        context.config.disableAutocompleteCache && builder.autocompleteCache?.force != true -> NoCacheAutocomplete
        else -> AbstractAutocompleteCache.fromMode(this)
    }

    @JvmSynthetic
    internal fun invalidate() {
        cache.invalidate()
    }
}
