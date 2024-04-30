package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.internal.components.AbstractAwaitableComponent
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

class EntitySelectMenu internal constructor(
    componentController: ComponentController,
    override val internalId: Int,
    private val selectMenu: JDAEntitySelectMenu
) : AbstractAwaitableComponent<EntitySelectEvent>(componentController),
    JDAEntitySelectMenu by selectMenu,
    IdentifiableComponent {

    override fun asEnabled(): JDAEntitySelectMenu = withDisabled(false)

    override fun asDisabled(): JDAEntitySelectMenu = withDisabled(true)

    override fun withDisabled(disabled: Boolean): EntitySelectMenu {
        return EntitySelectMenu(componentController, internalId, super.withDisabled(disabled))
    }

    override fun getId(): String = selectMenu.id ?: throwInternal("BC components cannot have null IDs")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntitySelectMenu

        return selectMenu == other.selectMenu
    }

    override fun hashCode(): Int {
        return selectMenu.hashCode()
    }

    override fun toString(): String {
        return selectMenu.toString()
    }
}