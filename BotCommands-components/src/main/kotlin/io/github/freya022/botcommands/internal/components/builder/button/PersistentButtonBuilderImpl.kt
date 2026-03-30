package io.github.freya022.botcommands.internal.components.builder.button

import io.github.freya022.botcommands.api.components.builder.button.PersistentButtonBuilder
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.IPersistentActionableComponentMixin
import io.github.freya022.botcommands.internal.components.builder.mixin.IPersistentTimeoutableComponentMixin
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.PersistentActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji

@PublishedApi
internal class PersistentButtonBuilderImpl internal constructor(
    componentController: ComponentController,
    style: ButtonStyle,
    label: String?,
    emoji: Emoji?,
    disabled: Boolean,
    instanceRetriever: InstanceRetriever<PersistentButtonBuilder>
) : AbstractButtonBuilder<PersistentButtonBuilder>(componentController, style, label, emoji, disabled, instanceRetriever),
    PersistentButtonBuilder,
    IPersistentActionableComponentMixin<PersistentButtonBuilder> by PersistentActionableComponentImpl(componentController.context, ComponentType.BUTTON, instanceRetriever),
    IPersistentTimeoutableComponentMixin<PersistentButtonBuilder> by PersistentTimeoutableComponentImpl(componentController.context, ComponentType.BUTTON, instanceRetriever) {

    override val lifetimeType: LifetimeType get() = LifetimeType.PERSISTENT
    override val instance: PersistentButtonBuilderImpl get() = this

    init {
        instanceRetriever.instance = this
    }
}
