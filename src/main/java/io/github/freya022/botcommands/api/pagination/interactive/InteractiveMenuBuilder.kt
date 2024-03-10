package io.github.freya022.botcommands.api.pagination.interactive

import io.github.freya022.botcommands.api.components.Components

/**
 * Builds an [InteractiveMenu]
 */
class InteractiveMenuBuilder internal constructor(
    componentsService: Components
) : BasicInteractiveMenuBuilder<InteractiveMenuBuilder, InteractiveMenu>(componentsService) {
    override fun build(): InteractiveMenu = InteractiveMenu(componentsService, this)
}
