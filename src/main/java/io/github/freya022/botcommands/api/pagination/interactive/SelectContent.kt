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
        fun of(label: String): SelectContent {
            return SelectContent(label, null, null)
        }

        @JvmStatic
        fun of(label: String, description: String?): SelectContent {
            return SelectContent(label, description, null)
        }

        @JvmStatic
        fun of(label: String, description: String?, emoji: Emoji?): SelectContent {
            return SelectContent(label, description, emoji)
        }
    }
}
