package io.github.freya022.botcommands.api.pagination.interactive

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.StringSelectMenu

/**
 * A type of pagination which shows embeds and provides a [StringSelectMenu] to navigate between menus.
 *
 * Each embed is bound to a selection menu.
 *
 * *This does not provide pagination for each embed* (no arrow buttons, only the selection menu).
 */
class InteractiveMenu internal constructor(
    componentsService: Components,
    builder: InteractiveMenuBuilder
) : BasicInteractiveMenu<InteractiveMenu>(
    componentsService,
    builder
)
