package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion
import net.dv8tion.jda.api.components.selects.EntitySelectMenu as JDAEntitySelectMenu

@CustomJDAComponent(net.dv8tion.jda.internal.components.selects.EntitySelectMenuImpl::class)
internal class EntitySelectMenuImpl internal constructor(
    componentController: ComponentController,
    override val internalId: Int,
    private val selectMenu: JDAEntitySelectMenu
) : AbstractAwaitableComponentImpl<EntitySelectEvent>(componentController),
    EntitySelectMenu,
    JDAEntitySelectMenu by selectMenu,
    ActionRowChildComponentUnion {

    override fun withDisabled(disabled: Boolean): EntitySelectMenuImpl {
        return EntitySelectMenuImpl(componentController, internalId, super<JDAEntitySelectMenu>.withDisabled(disabled))
    }

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