package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.PersistentActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class PersistentButtonBuilder internal constructor(
    style: ButtonStyle,
    componentController: ComponentController,
    label: String?,
    emoji: Emoji?
) : AbstractButtonBuilder<PersistentButtonBuilder>(componentController, style, label, emoji),
    IPersistentActionableComponent<PersistentButtonBuilder> by PersistentActionableComponentImpl(componentController.context),
    IPersistentTimeoutableComponent<PersistentButtonBuilder> by PersistentTimeoutableComponentImpl() {

    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
    override val instance: PersistentButtonBuilder = this
}