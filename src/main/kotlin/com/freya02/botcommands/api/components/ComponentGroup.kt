package com.freya02.botcommands.api.components

import com.freya02.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

class ComponentGroup internal constructor(private val componentController: ComponentController, private val id: String) : IdentifiableComponent {
    override fun getId(): String = id

    /**
     * **Awaiting on a component that is part of a group is undefined behavior**
     *
     * @throws TimeoutCancellationException If the timeout set in the component builder has been reached
     */
    @JvmSynthetic
    suspend fun await(): GenericComponentInteractionCreateEvent = componentController.awaitComponent(this)
}