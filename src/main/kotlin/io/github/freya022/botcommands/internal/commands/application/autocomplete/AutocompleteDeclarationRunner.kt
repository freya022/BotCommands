package io.github.freya022.botcommands.internal.commands.application.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteDeclaration
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteManagerImpl

@BService
internal class AutocompleteDeclarationRunner(
    context: BContext,
    autocompleteDeclarations: List<AutocompleteDeclaration>
) {
    init {
        val manager = AutocompleteManagerImpl(context)
        autocompleteDeclarations.forEach { it.declare(manager) }
    }
}