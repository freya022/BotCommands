package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

class EntitySelectMenu internal constructor(
    private val componentController: ComponentController,
    private val selectMenu: JDAEntitySelectMenu
) : JDAEntitySelectMenu by selectMenu, IdentifiableComponent {
    override fun withDisabled(disabled: Boolean): EntitySelectMenu {
        return EntitySelectMenu(componentController, super.withDisabled(disabled))
    }

    @JvmSynthetic
    override suspend fun await(): EntitySelectEvent = componentController.awaitComponent(this)

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