package com.freya02.botcommands.api.new_components.builder.button

import com.freya02.botcommands.api.new_components.builder.ComponentBuilder
import com.freya02.botcommands.api.utils.ButtonContent
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button

interface ButtonBuilder<T : ButtonBuilder<T>> : ComponentBuilder<T> {
    fun build(label: String): Button = build(label, null)
    fun build(emoji: Emoji): Button = build(null, emoji)
    fun build(content: ButtonContent): Button = build(content.text, content.emoji)
    fun build(label: String?, emoji: Emoji?): Button
}