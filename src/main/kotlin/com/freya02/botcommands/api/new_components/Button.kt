package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.new_components.new.ComponentController
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.buttons.Button as JDAButton

class Button internal constructor(private val componentController: ComponentController, button: JDAButton) : JDAButton by button, IdentifiableComponent {
    override fun withDisabled(disabled: Boolean): Button {
        return Button(componentController, super.withDisabled(disabled))
    }

    override fun withEmoji(emoji: Emoji?): Button {
        return Button(componentController, super.withEmoji(emoji))
    }

    override fun withLabel(label: String): Button {
        return Button(componentController, super.withLabel(label))
    }

    override fun withId(id: String): Button {
        return Button(componentController, super.withId(id))
    }

    override fun withUrl(url: String): Button {
        return Button(componentController, super.withUrl(url))
    }

    override fun withStyle(style: ButtonStyle): Button {
        return Button(componentController, super.withStyle(style))
    }

    /**
     * If the button or the group has it's timeout reached then this throws [TimeoutCancellationException]
     */
    @JvmSynthetic
    suspend fun await(): ButtonEvent = componentController.awaitComponent(this)
}