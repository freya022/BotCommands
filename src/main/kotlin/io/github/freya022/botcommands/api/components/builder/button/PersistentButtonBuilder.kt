package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.PersistentActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class PersistentButtonBuilder internal constructor(
    componentController: ComponentController,
    style: ButtonStyle,
    label: String?,
    emoji: Emoji?,
    instanceRetriever: InstanceRetriever<PersistentButtonBuilder>
) : AbstractButtonBuilder<PersistentButtonBuilder>(componentController, style, label, emoji, instanceRetriever),
    IPersistentActionableComponent<PersistentButtonBuilder> by PersistentActionableComponentImpl(componentController.context, instanceRetriever),
    IPersistentTimeoutableComponent<PersistentButtonBuilder> by PersistentTimeoutableComponentImpl(instanceRetriever) {

    override val lifetimeType: LifetimeType get() = LifetimeType.PERSISTENT
    override val instance: PersistentButtonBuilder get() = this

    init {
        instanceRetriever.instance = this
    }
}