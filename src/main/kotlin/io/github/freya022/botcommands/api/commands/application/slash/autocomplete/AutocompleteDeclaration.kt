package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

@InterfacedService(acceptMultiple = true)
interface AutocompleteDeclaration {
    fun declare(manager: AutocompleteManager)
}