package io.github.freya022.botcommands.internal.components.builder.button

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.builder.button.ButtonBuilder
import io.github.freya022.botcommands.internal.components.ButtonImpl
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.builder.AbstractComponentBuilder
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

@PublishedApi
internal sealed class AbstractButtonBuilder<T : ButtonBuilder<T>>(
    private val componentController: ComponentController,
    private val style: ButtonStyle,
    private val label: String?,
    private val emoji: Emoji?,
    instanceRetriever: InstanceRetriever<T>
) : AbstractComponentBuilder<T>(instanceRetriever),
    ButtonBuilder<T> {

    final override val componentType: ComponentType = ComponentType.BUTTON

    private var built = false

    override fun build(): Button = runBlocking { buildSuspend() }

    @PublishedApi
    internal suspend fun buildSuspend(): Button {
        check(!built) { "Cannot build components more than once" }
        built = true

        return componentController.withNewComponent(this) { internalId, componentId ->
            ButtonImpl(
                componentController,
                internalId,
                net.dv8tion.jda.api.interactions.components.buttons.Button.of(style, componentId, label, emoji)
            )
        }
    }
}