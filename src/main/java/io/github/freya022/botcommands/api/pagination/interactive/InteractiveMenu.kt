package io.github.freya022.botcommands.api.pagination.interactive

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.pagination.TimeoutInfo
import io.github.freya022.botcommands.api.utils.ButtonContent

/**
 * A type of pagination which shows embeds and provides a [StringSelectMenu] to navigate between menus.
 *
 * Each embed is bound to a selection menu.
 *
 * *This does not provide pagination for each embed* (no arrow buttons, only the selection menu).
 */
class InteractiveMenu internal constructor(
    componentsService: Components,
    constraints: InteractionConstraints,
    timeout: TimeoutInfo<InteractiveMenu>?,
    hasDeleteButton: Boolean,
    firstContent: ButtonContent,
    previousContent: ButtonContent,
    nextContent: ButtonContent,
    lastContent: ButtonContent,
    deleteContent: ButtonContent,
    items: List<InteractiveMenuItem<InteractiveMenu>>,
    usePaginator: Boolean
) : BasicInteractiveMenu<InteractiveMenu>(
    componentsService,
    constraints,
    timeout,
    hasDeleteButton,
    firstContent,
    previousContent,
    nextContent,
    lastContent,
    deleteContent,
    items,
    usePaginator
)
