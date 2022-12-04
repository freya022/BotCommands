package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.internal.new_components.new.ComponentController
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

class ComponentGroup internal constructor(private val componentController: ComponentController, private val id: String, private val componentIds: List<String>) : IdentifiableComponent {
    override fun getId(): String = id

    /**
     * If the button or the group has it's timeout reached then this throws [TimeoutCancellationException]
     */
    @JvmSynthetic
    suspend fun await(): GenericComponentInteractionCreateEvent = componentController.awaitComponent(this)
}