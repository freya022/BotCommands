package com.freya02.botcommands.api.components

import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.internal.new_components.new.ComponentController
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

class EntitySelectMenu internal constructor(
    private val componentController: ComponentController,
    selectMenu: JDAEntitySelectMenu
) : JDAEntitySelectMenu by selectMenu, IdentifiableComponent {
    override fun withDisabled(disabled: Boolean): EntitySelectMenu {
        return EntitySelectMenu(componentController, super.withDisabled(disabled))
    }

    /**
     * **Awaiting on a component that is part of a group is undefined behavior**
     *
     * @throws TimeoutCancellationException If the timeout set in the component builder has been reached
     */
    @JvmSynthetic
    suspend fun await(): EntitySelectEvent = componentController.awaitComponent(this)
}