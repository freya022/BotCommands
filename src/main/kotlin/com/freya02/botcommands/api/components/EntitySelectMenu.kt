package com.freya02.botcommands.api.components

import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

class EntitySelectMenu internal constructor(
    private val componentController: ComponentController,
    selectMenu: JDAEntitySelectMenu
) : JDAEntitySelectMenu by selectMenu, IdentifiableComponent {
    override fun withDisabled(disabled: Boolean): EntitySelectMenu {
        return EntitySelectMenu(componentController, super.withDisabled(disabled))
    }

    @JvmSynthetic
    override suspend fun await(): EntitySelectEvent = componentController.awaitComponent(this)
}