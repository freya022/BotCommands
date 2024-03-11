package io.github.freya022.botcommands.api.pagination.interactive

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.selections.SelectOption

data class SelectContent(val label: String, val description: String?, val emoji: Emoji?) {
    fun toSelectOption(value: String): SelectOption {
        var selectOption = SelectOption.of(label, value)
        description?.let { selectOption = selectOption.withDescription(description) }
        emoji?.let { selectOption = selectOption.withEmoji(emoji) }

        return selectOption
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun of(label: String, description: String? = null, emoji: Emoji? = null): SelectContent {
            return SelectContent(label, description, emoji)
        }
    }
}
