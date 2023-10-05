package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.builder.AbstractComponentBuilder
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.buttons.Button as JDAButton

abstract class AbstractButtonBuilder internal constructor(
    private val componentController: ComponentController,
    private val style: ButtonStyle
) : AbstractComponentBuilder() {
    final override val componentType: ComponentType = ComponentType.BUTTON

    @JvmSynthetic
    internal fun build(label: String?, emoji: Emoji?): Button {
        return Button(componentController, JDAButton.of(style, componentController.createComponent(this), label, emoji))
    }
}