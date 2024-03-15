package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.builder.IEphemeralActionableComponent
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.EphemeralActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class EphemeralButtonBuilder internal constructor(
    componentController: ComponentController,
    style: ButtonStyle,
    label: String?,
    emoji: Emoji?,
    instanceRetriever: InstanceRetriever<EphemeralButtonBuilder>
) : AbstractButtonBuilder<EphemeralButtonBuilder>(componentController, style, label, emoji, instanceRetriever),
    IEphemeralActionableComponent<EphemeralButtonBuilder, ButtonEvent> by EphemeralActionableComponentImpl(componentController.context, instanceRetriever),
    IEphemeralTimeoutableComponent<EphemeralButtonBuilder> by EphemeralTimeoutableComponentImpl(instanceRetriever) {

    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
    override val instance: EphemeralButtonBuilder = this

    init {
        instanceRetriever.instance = this
    }
}