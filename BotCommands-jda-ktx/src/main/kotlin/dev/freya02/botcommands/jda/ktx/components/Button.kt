package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji

fun link(url: String, label: String? = null, emoji: Emoji? = null, disabled: Boolean = false): Button {
    return Button.of(ButtonStyle.LINK, url, label, emoji).withDisabled(disabled)
}
