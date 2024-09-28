package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.buttons.Button as JDAButton

internal class ButtonImpl internal constructor(
    componentController: ComponentController,
    override val internalId: Int,
    private val button: JDAButton
) : AbstractAwaitableComponentImpl<ButtonEvent>(componentController),
    Button,
    JDAButton by button {

    override fun withDisabled(disabled: Boolean): ButtonImpl {
        return ButtonImpl(componentController, internalId, super<JDAButton>.withDisabled(disabled))
    }

    override fun withEmoji(emoji: Emoji?): ButtonImpl {
        return ButtonImpl(componentController, internalId, super<JDAButton>.withEmoji(emoji))
    }

    override fun withLabel(label: String): ButtonImpl {
        return ButtonImpl(componentController, internalId, super<JDAButton>.withLabel(label))
    }

    override fun withStyle(style: ButtonStyle): ButtonImpl {
        return ButtonImpl(componentController, internalId, super<JDAButton>.withStyle(style))
    }

    override fun getId(): String = button.id ?: throwInternal("BC components cannot have null IDs")

    override fun getUrl(): String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ButtonImpl

        return button == other.button
    }

    override fun hashCode(): Int {
        return button.hashCode()
    }

    override fun toString(): String {
        return button.toString()
    }
}