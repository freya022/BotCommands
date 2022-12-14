package com.freya02.botcommands.api.components

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.components.new.ComponentController
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
     * **Awaiting on a component that is part of a group is undefined behavior**
     *
     * @throws TimeoutCancellationException If the timeout set in the component builder has been reached
     */
    @JvmSynthetic
    suspend fun await(): ButtonEvent = componentController.awaitComponent(this)
}