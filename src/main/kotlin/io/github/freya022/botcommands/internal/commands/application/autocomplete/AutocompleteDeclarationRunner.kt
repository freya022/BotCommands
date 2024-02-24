package io.github.freya022.botcommands.internal.commands.application.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteDeclaration
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteManagerImpl
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService(priority = 1) //Higher than all application command declarations
internal class AutocompleteDeclarationRunner internal constructor(
    context: BContext,
    autocompleteDeclarations: List<AutocompleteDeclaration>,
    autocompleteInfoContainer: AutocompleteInfoContainer
) {
    init {
        val manager = AutocompleteManagerImpl(context)
        autocompleteDeclarations.forEach { it.declareAutocomplete(manager) }

        if (logger.isTraceEnabled()) {
            logger.trace {
                "Registered ${autocompleteInfoContainer.size} autocomplete handlers:\n${autocompleteInfoContainer.allInfos.joinAsList()}"
            }
        } else {
            logger.debug { "Registered ${autocompleteInfoContainer.size} autocomplete handlers" }
        }
    }
}