package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Interface to declare autocomplete handlers, ran once at startup.
 *
 * **Usage**: Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see AutocompleteTransformer
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface AutocompleteDeclaration {
    fun declareAutocomplete(manager: AutocompleteManager)
}