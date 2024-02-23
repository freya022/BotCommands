package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteManager
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.application.autocomplete.AutocompleteInfoContainer
import kotlin.reflect.KFunction

internal class AutocompleteManagerImpl internal constructor(override val context: BContext) : AutocompleteManager {
    private val autocompleteInfoContainer = context.getService<AutocompleteInfoContainer>()

    override fun autocomplete(name: String, function: KFunction<Collection<Any>>, block: AutocompleteInfoBuilder.() -> Unit) {
        autocompleteInfoContainer += AutocompleteInfoBuilder(context, name, function).apply(block).build()
    }
}