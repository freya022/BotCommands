package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.builder.IEphemeralActionableComponent
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.EphemeralActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class EphemeralButtonBuilder internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : AbstractButtonBuilder(componentController, style),
    IEphemeralActionableComponent<ButtonEvent> by EphemeralActionableComponentImpl(),
    IEphemeralTimeoutableComponent by EphemeralTimeoutableComponentImpl() {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}