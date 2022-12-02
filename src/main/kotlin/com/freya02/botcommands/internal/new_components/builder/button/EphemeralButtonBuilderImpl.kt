package com.freya02.botcommands.internal.new_components.builder.button

import com.freya02.botcommands.api.new_components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.api.new_components.builder.IEphemeralTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.button.EphemeralButtonBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.builder.EphemeralActionableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.EphemeralTimeoutableComponentImpl
import com.freya02.botcommands.internal.new_components.new.ComponentController
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

internal class EphemeralButtonBuilderImpl internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : ButtonBuilderImpl<EphemeralButtonBuilder>(componentController, style),
    EphemeralButtonBuilder,
    IEphemeralActionableComponent<EphemeralButtonBuilder> by EphemeralActionableComponentImpl(),
    IEphemeralTimeoutableComponent<EphemeralButtonBuilder> by EphemeralTimeoutableComponentImpl() {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}