package com.freya02.botcommands.api.components

import com.freya02.botcommands.api.components.event.StringSelectEvent
import com.freya02.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

class StringSelectMenu internal constructor(
    private val componentController: ComponentController,
    selectMenu: JDAStringSelectMenu
) : JDAStringSelectMenu by selectMenu, IdentifiableComponent {
    override fun withDisabled(disabled: Boolean): StringSelectMenu {
        return StringSelectMenu(componentController, super.withDisabled(disabled))
    }

    /**
     * **Awaiting on a component that is part of a group is undefined behavior**
     *
     * @throws TimeoutCancellationException If the timeout set in the component builder has been reached
     */
    @JvmSynthetic
    suspend fun await(): StringSelectEvent = componentController.awaitComponent(this)
}