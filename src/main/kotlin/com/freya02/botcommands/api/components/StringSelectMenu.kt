package com.freya02.botcommands.api.components

import com.freya02.botcommands.api.components.event.StringSelectEvent
import com.freya02.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

class StringSelectMenu internal constructor(
    private val componentController: ComponentController,
    selectMenu: JDAStringSelectMenu
) : JDAStringSelectMenu by selectMenu, IdentifiableComponent {
    override fun withDisabled(disabled: Boolean): StringSelectMenu {
        return StringSelectMenu(componentController, super.withDisabled(disabled))
    }

    @JvmSynthetic
    override suspend fun await(): StringSelectEvent = componentController.awaitComponent(this)
}