package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.caches.AbstractAutocompleteCache
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.caches.NoCacheAutocomplete
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

//See AutocompleteHandler for implementation details
internal class AutocompleteInfoImpl internal constructor(
    context: BContext,
    builder: AutocompleteInfoBuilderImpl
) : AutocompleteInfo() {
    override val declarationSite: DeclarationSite = builder.declarationSite
    override val name: String? = builder.name
    internal val eventFunction = builder.function.toMemberParamFunction<CommandAutoCompleteInteractionEvent, _>(context)
    override val function get() = eventFunction.kFunction
    override val mode: AutocompleteMode = builder.mode
    override val showUserInput: Boolean = builder.showUserInput

    override val autocompleteCache: AutocompleteCacheInfo? = builder.autocompleteCache

    internal val cache = when {
        context.applicationConfig.disableAutocompleteCache && builder.autocompleteCache?.force != true -> NoCacheAutocomplete
        else -> AbstractAutocompleteCache.fromMode(this)
    }

    override fun invalidate() {
        cache.invalidate()
    }

    override fun toString(): String = "AutocompleteInfo(name=$name)"
}
