package com.freya02.botcommands.api.components.builder.button

import com.freya02.botcommands.api.components.builder.IPersistentActionableComponent
import com.freya02.botcommands.api.components.builder.IPersistentTimeoutableComponent
import com.freya02.botcommands.internal.components.LifetimeType
import com.freya02.botcommands.internal.components.builder.PersistentActionableComponentImpl
import com.freya02.botcommands.internal.components.builder.PersistentTimeoutableComponentImpl
import com.freya02.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class PersistentButtonBuilder internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : AbstractButtonBuilder(componentController, style),
    IPersistentActionableComponent by PersistentActionableComponentImpl(),
    IPersistentTimeoutableComponent by PersistentTimeoutableComponentImpl() {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
}