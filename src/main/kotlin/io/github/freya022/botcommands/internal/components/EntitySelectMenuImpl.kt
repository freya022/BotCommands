package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

internal class EntitySelectMenuImpl internal constructor(
    componentController: ComponentController,
    override val internalId: Int,
    private val selectMenu: JDAEntitySelectMenu
) : AbstractAwaitableComponentImpl<EntitySelectEvent>(componentController),
    EntitySelectMenu,
    JDAEntitySelectMenu by selectMenu {

    override fun withDisabled(disabled: Boolean): EntitySelectMenuImpl {
        return EntitySelectMenuImpl(componentController, internalId, super<JDAEntitySelectMenu>.withDisabled(disabled))
    }

    override fun getId(): String = selectMenu.id ?: throwInternal("BC components cannot have null IDs")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntitySelectMenuImpl

        return selectMenu == other.selectMenu
    }

    override fun hashCode(): Int {
        return selectMenu.hashCode()
    }

    override fun toString(): String {
        return selectMenu.toString()
    }
}