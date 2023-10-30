package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.builder.IEphemeralActionableComponent
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.EphemeralActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class EphemeralButtonBuilder internal constructor(
    style: ButtonStyle,
    componentController: ComponentController,
    label: String?,
    emoji: Emoji?
) : AbstractButtonBuilder(componentController, style, label, emoji),
    IEphemeralActionableComponent<ButtonEvent> by EphemeralActionableComponentImpl(componentController.context),
    IEphemeralTimeoutableComponent by EphemeralTimeoutableComponentImpl() {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}