package com.freya02.botcommands.api.components.builder.button

import com.freya02.botcommands.api.components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.components.LifetimeType
import com.freya02.botcommands.internal.components.builder.EphemeralActionableComponentImpl
import com.freya02.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl
import com.freya02.botcommands.internal.components.new.ComponentController
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class EphemeralButtonBuilder internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : AbstractButtonBuilder(componentController, style),
    IEphemeralActionableComponent<ButtonEvent> by EphemeralActionableComponentImpl(),
    IEphemeralTimeoutableComponent by EphemeralTimeoutableComponentImpl() {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}