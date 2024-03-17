package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.builder.select.EntitySelectMenuFactory
import io.github.freya022.botcommands.api.components.builder.select.StringSelectMenuFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import java.util.*
import javax.annotation.CheckReturnValue

/**
 * @see Components
 * @see Buttons
 */
@BService
@Dependencies(Components::class)
class SelectMenus internal constructor(componentController: ComponentController) : AbstractComponentFactory(componentController) {
    /**
     * Creates a [StringSelectMenu] builder factory.
     *
     * You can use [StringSelectMenuFactory.persistent] or [StringSelectMenuFactory.ephemeral]
     * to then start building a string select menu.
     */
    @CheckReturnValue
    fun stringSelectMenu(): StringSelectMenuFactory = StringSelectMenuFactory(componentController)

    /**
     * Creates a [EntitySelectMenu] builder factory.
     *
     * You can use [EntitySelectMenuFactory.persistent] or [EntitySelectMenuFactory.ephemeral]
     * to then start building an entity select menu.
     */
    @CheckReturnValue
    fun entitySelectMenu(target: SelectTarget, vararg targets: SelectTarget): EntitySelectMenuFactory =
        entitySelectMenu(EnumSet.of(target, *targets))

    /**
     * Creates a [EntitySelectMenu] builder factory.
     *
     * You can use [EntitySelectMenuFactory.persistent] or [EntitySelectMenuFactory.ephemeral]
     * to then start building an entity select menu.
     */
    @CheckReturnValue
    fun entitySelectMenu(targets: Collection<SelectTarget>): EntitySelectMenuFactory =
        EntitySelectMenuFactory(componentController, targets)
}