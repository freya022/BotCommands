package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Interface to declare autocomplete handlers, ran once at startup.
 *
 * **Usage**: Register your instance as a service with [@BService][BService].
 *
 * @see AutocompleteHandler @AutocompleteHandler
 * @see AutocompleteTransformer
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface AutocompleteHandlerProvider {
    fun declareAutocomplete(manager: AutocompleteManager)
}