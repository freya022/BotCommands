package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.buttons.Button as JDAButton

class Button internal constructor(
    private val componentController: ComponentController,
    private val button: JDAButton
) : JDAButton by button, IdentifiableComponent {
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
    override suspend fun await(): ButtonEvent = componentController.awaitComponent(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Button

        return button == other.button
    }

    override fun hashCode(): Int {
        return button.hashCode()
    }

    override fun toString(): String {
        return button.toString()
    }
}