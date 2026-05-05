package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.config.applicationConfig
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.NoCacheAutocomplete
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.suppliers.ChoiceSupplierFactory
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
    internal val methodAccessor get() = eventFunction.methodAccessor
    override val showUserInput: Boolean = builder.showUserInput

    internal val choiceSupplier = context.getService<ChoiceSupplierFactory>().create(function, builder.mode, showUserInput)

    internal val cache = when (val autocompleteCacheFactory = builder.autocompleteCacheFactory) {
        null -> NoCacheAutocomplete
        else if context.applicationConfig.disableAutocompleteCache && !autocompleteCacheFactory.force -> NoCacheAutocomplete
        else -> autocompleteCacheFactory.create()
    }

    override fun invalidate() {
        cache.invalidate()
    }

    override fun toString(): String = "AutocompleteInfo(name=$name)"
}
