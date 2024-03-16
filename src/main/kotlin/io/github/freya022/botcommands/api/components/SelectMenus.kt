package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.builder.select.EntitySelectMenuFactory
import io.github.freya022.botcommands.api.components.builder.select.StringSelectMenuFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import java.util.*
import javax.annotation.CheckReturnValue

/**
 * @see Components
 * @see Buttons
 */
@BService
@Dependencies(Components::class)
class SelectMenus internal constructor(componentController: ComponentController) : AbstractComponentFactory(componentController) {
    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    @CheckReturnValue
    fun stringSelectMenu(): StringSelectMenuFactory = StringSelectMenuFactory(componentController)

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @CheckReturnValue
    fun entitySelectMenu(target: EntitySelectMenu.SelectTarget, vararg targets: EntitySelectMenu.SelectTarget): EntitySelectMenuFactory =
        entitySelectMenu(EnumSet.of(target, *targets))

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @CheckReturnValue
    fun entitySelectMenu(targets: Collection<EntitySelectMenu.SelectTarget>): EntitySelectMenuFactory =
        EntitySelectMenuFactory(componentController, targets)
}