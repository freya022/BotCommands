package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.declaration

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteHandlerProvider
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoContainer
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService(priority = 1) //Higher than all application command declarations
@RequiresApplicationCommands
internal class AutocompleteDeclarationRunner internal constructor(
    context: BContext,
    autocompleteHandlerProviders: List<AutocompleteHandlerProvider>,
    autocompleteInfoContainer: AutocompleteInfoContainer
) {
    init {
        val manager = AutocompleteManagerImpl(context)
        autocompleteHandlerProviders.forEach { it.declareAutocomplete(manager) }

        if (logger.isTraceEnabled()) {
            logger.trace {
                val declarations = autocompleteInfoContainer.allInfos.joinAsList { it.declarationSite.toString() }
                "Registered ${autocompleteInfoContainer.size} autocomplete handlers:\n$declarations"
            }
        } else {
            logger.debug { "Registered ${autocompleteInfoContainer.size} autocomplete handlers" }
        }
    }
}