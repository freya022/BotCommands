package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.PersistentActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class PersistentButtonBuilder internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : AbstractButtonBuilder(componentController, style),
    IPersistentActionableComponent by PersistentActionableComponentImpl(),
    IPersistentTimeoutableComponent by PersistentTimeoutableComponentImpl() {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
}