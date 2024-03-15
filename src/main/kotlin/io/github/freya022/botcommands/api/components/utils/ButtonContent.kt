package io.github.freya022.botcommands.api.components.utils

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.utils.EmojiUtils
import net.dv8tion.jda.api.entities.emoji.Emoji

/**
 * Represents the visual content of a [Button], this contains at least an [Emoji] or a [String]
 */
data class ButtonContent(val label: String?, val emoji: Emoji?) {
    init {
        require(label != null || emoji != null) { "A label or an emoji needs to be set" }
    }

    companion object {
        /**
         * Constructs a [ButtonContent] with a label.
         */
        @JvmStatic
        fun fromLabel(label: String): ButtonContent {
            return ButtonContent(label, null)
        }

        /**
         * Constructs a [ButtonContent] with an [Emoji].
         */
        @JvmStatic
        fun fromEmoji(emoji: Emoji): ButtonContent {
            return ButtonContent(null, emoji)
        }

        /**
         * Constructs a [ButtonContent] with a label and an [Emoji].
         */
        @JvmStatic
        fun fromEmoji(label: String, emoji: Emoji): ButtonContent {
            return ButtonContent(label, emoji)
        }

        /**
         * Constructs a [ButtonContent] with an unicode emoji,
         * see [Emoji.fromUnicode] for accepted formats.
         */
        @JvmStatic
        fun fromUnicode(unicode: String): ButtonContent {
            return ButtonContent(null, Emoji.fromUnicode(unicode))
        }

        /**
         * Constructs a [ButtonContent] with a label and an unicode emoji,
         * see [Emoji.fromUnicode] for accepted formats.
         */
        @JvmStatic
        fun fromUnicode(label: String, unicode: String): ButtonContent {
            return ButtonContent(label, Emoji.fromUnicode(unicode))
        }

        /**
         * Constructs a [ButtonContent] from a shortcode emoji, such as `:joy:`.
         */
        @JvmStatic
        fun fromShortcode(shortcode: String): ButtonContent {
            return ButtonContent(null, EmojiUtils.resolveJDAEmoji(shortcode))
        }

        /**
         * Constructs a [ButtonContent] from a [String] and a shortcode emoji, such as `:joy:`.
         */
        @JvmStatic
        fun fromShortcode(text: String, shortcode: String): ButtonContent {
            return ButtonContent(text, EmojiUtils.resolveJDAEmoji(shortcode))
        }
    }
}
