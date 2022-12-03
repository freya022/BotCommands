package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.api.components.event.StringSelectionEvent
import com.freya02.botcommands.internal.new_components.new.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

class StringSelectMenu internal constructor(
    private val componentController: ComponentController,
    selectMenu: JDAStringSelectMenu
) : JDAStringSelectMenu by selectMenu, IdentifiableComponent {
    override fun withDisabled(disabled: Boolean): StringSelectMenu {
        return StringSelectMenu(componentController, super.withDisabled(disabled))
    }

    suspend fun await(): StringSelectionEvent = componentController.awaitComponent(this)
}