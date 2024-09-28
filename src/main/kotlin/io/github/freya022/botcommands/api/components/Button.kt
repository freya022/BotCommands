package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.event.ButtonEvent
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.buttons.Button as JDAButton

interface Button : JDAButton,
                   AwaitableComponent<ButtonEvent>,
                   IGroupHolder {

    override fun asDisabled(): Button = withDisabled(true)

    override fun asEnabled(): Button = withDisabled(false)

    override fun withDisabled(disabled: Boolean): Button

    override fun withEmoji(emoji: Emoji?): Button

    override fun withLabel(label: String): Button

    override fun withId(id: String): Nothing =
        throw UnsupportedOperationException("This type of button cannot contain custom IDs")

    override fun withUrl(url: String): Nothing =
        throw UnsupportedOperationException("This type of button cannot contain URLs")

    override fun withStyle(style: ButtonStyle): Button

    override fun getId(): String
}