package io.github.freya022.botcommands.internal.components.builder.button

import io.github.freya022.botcommands.api.components.builder.button.EphemeralButtonBuilder
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.*
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

internal class EphemeralButtonBuilderImpl internal constructor(
    componentController: ComponentController,
    style: ButtonStyle,
    label: String?,
    emoji: Emoji?,
    instanceRetriever: InstanceRetriever<EphemeralButtonBuilder>
) : AbstractButtonBuilder<EphemeralButtonBuilder>(componentController, style, label, emoji, instanceRetriever),
    EphemeralButtonBuilder,
    IEphemeralActionableComponentMixin<EphemeralButtonBuilder, ButtonEvent> by EphemeralActionableComponentImpl(
        componentController.context,
        instanceRetriever
    ),
    IEphemeralTimeoutableComponentMixin<EphemeralButtonBuilder> by EphemeralTimeoutableComponentImpl(instanceRetriever) {

    override val lifetimeType: LifetimeType get() = LifetimeType.EPHEMERAL
    override val instance: EphemeralButtonBuilderImpl get() = this

    init {
        instanceRetriever.instance = this
    }
}