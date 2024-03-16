package io.github.freya022.botcommands.api.components.utils

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.utils.EmojiUtils
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

/**
 * Represents the visual content of a [Button], this contains at least an [Emoji] or a [String]
 */
data class ButtonContent(val style: ButtonStyle, val label: String?, val emoji: Emoji?) {
    init {
        require(label != null || emoji != null) { "A label or an emoji needs to be set" }

        if (label != null) {
            require(label.isNotBlank()) {
                "The label cannot be blank"
            }
        }
    }

    companion object {
        /**
         * Constructs a [ButtonContent] with a label.
         */
        @JvmStatic
        fun fromLabel(style: ButtonStyle, label: String): ButtonContent {
            return ButtonContent(style, label, null)
        }

        /**
         * Constructs a [ButtonContent] with an [Emoji].
         */
        @JvmStatic
        fun fromEmoji(style: ButtonStyle, emoji: Emoji): ButtonContent {
            return ButtonContent(style, null, emoji)
        }

        /**
         * Constructs a [ButtonContent] with a label and an [Emoji].
         */
        @JvmStatic
        fun fromEmoji(style: ButtonStyle, label: String, emoji: Emoji): ButtonContent {
            return ButtonContent(style, label, emoji)
        }

        /**
         * Constructs a [ButtonContent] with an unicode emoji,
         * see [Emoji.fromUnicode] for accepted formats.
         */
        @JvmStatic
        fun fromUnicode(style: ButtonStyle, unicode: String): ButtonContent {
            return ButtonContent(style, null, Emoji.fromUnicode(unicode))
        }

        /**
         * Constructs a [ButtonContent] with a label and an unicode emoji,
         * see [Emoji.fromUnicode] for accepted formats.
         */
        @JvmStatic
        fun fromUnicode(style: ButtonStyle, label: String, unicode: String): ButtonContent {
            return ButtonContent(style, label, Emoji.fromUnicode(unicode))
        }

        /**
         * Constructs a [ButtonContent] from a shortcode emoji, such as `:joy:`.
         */
        @JvmStatic
        fun fromShortcode(style: ButtonStyle, shortcode: String): ButtonContent {
            return ButtonContent(style, null, EmojiUtils.resolveJDAEmoji(shortcode))
        }

        /**
         * Constructs a [ButtonContent] from a [String] and a shortcode emoji, such as `:joy:`.
         */
        @JvmStatic
        fun fromShortcode(style: ButtonStyle, text: String, shortcode: String): ButtonContent {
            return ButtonContent(style, text, EmojiUtils.resolveJDAEmoji(shortcode))
        }
    }
}
