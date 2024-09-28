package io.github.freya022.botcommands.internal.components.builder.button

import io.github.freya022.botcommands.api.components.builder.button.PersistentButtonBuilder
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.*
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

internal class PersistentButtonBuilderImpl internal constructor(
    componentController: ComponentController,
    style: ButtonStyle,
    label: String?,
    emoji: Emoji?,
    instanceRetriever: InstanceRetriever<PersistentButtonBuilder>
) : AbstractButtonBuilder<PersistentButtonBuilder>(componentController, style, label, emoji, instanceRetriever),
    PersistentButtonBuilder,
    IPersistentActionableComponentMixin<PersistentButtonBuilder> by PersistentActionableComponentImpl(
        componentController.context,
        instanceRetriever
    ),
    IPersistentTimeoutableComponentMixin<PersistentButtonBuilder> by PersistentTimeoutableComponentImpl(
        instanceRetriever
    ) {

    override val lifetimeType: LifetimeType get() = LifetimeType.PERSISTENT
    override val instance: PersistentButtonBuilderImpl get() = this

    init {
        instanceRetriever.instance = this
    }
}