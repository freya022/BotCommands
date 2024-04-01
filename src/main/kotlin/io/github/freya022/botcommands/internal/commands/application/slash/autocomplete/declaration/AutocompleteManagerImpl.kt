package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.declaration

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteManager
import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoContainer
import kotlin.reflect.KFunction

internal class AutocompleteManagerImpl internal constructor(override val context: BContext) : AutocompleteManager {
    private val autocompleteInfoContainer = context.getService<AutocompleteInfoContainer>()

    override fun autocomplete(function: KFunction<Collection<Any>>, name: String?, block: AutocompleteInfoBuilder.() -> Unit) {
        autocompleteInfoContainer += AutocompleteInfoBuilder(context, name, function)
            .setCallerAsDeclarationSite()
            .apply(block)
            .build()
    }
}