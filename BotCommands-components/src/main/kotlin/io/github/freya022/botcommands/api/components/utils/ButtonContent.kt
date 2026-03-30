package io.github.freya022.botcommands.api.components.utils

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.utils.EmojiUtils
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import javax.annotation.CheckReturnValue

/**
 * Represents the visual content of a [Button], this contains at least an [Emoji] or a [String]
 */
data class ButtonContent(val style: ButtonStyle, val label: String?, val emoji: Emoji?, val disabled: Boolean) {
    init {
        require(label != null || emoji != null) { "A label or an emoji needs to be set" }

        if (label != null) {
            require(label.isNotEmpty()) {
                "The label cannot be empty"
            }
        }
    }

    /**
     * Creates a new button content with the provided emoji alias, Unicode, or Markdown.
     *
     * ### Example
     * ```kt
     * // Emoji alias
     * withEmoji(":smiley:")
     * // Unicode emoji
     * withEmoji("😃")
     *
     * // Animated custom emoji
     * withEmoji("<a:dance:123456789123456789>")
     * // Not animated custom emoji
     * withEmoji("<:dog:123456789123456789>")
     *
     * // Unicode emoji, escape codes
     * withEmoji("&#92;uD83D&#92;uDE03")
     * // Codepoint notation
     * withEmoji("U+1F602")
     * ```
     */
    @CheckReturnValue
    fun withEmoji(aliasOrUnicode: String?): ButtonContent {
        val newEmoji = aliasOrUnicode?.let {
            EmojiUtils.resolveJDAEmojiOrNull(it) ?: Emoji.fromFormatted(it)
        }

        return ButtonContent(style, label, newEmoji, disabled)
    }

    /**
     * Creates a new button content with the provided JDA emoji.
     */
    @CheckReturnValue
    fun withEmoji(emoji: Emoji?): ButtonContent = ButtonContent(style, label, emoji, disabled)

    /**
     * Creates a new button content with the provided disabled state.
     */
    @CheckReturnValue
    fun withDisabled(disabled: Boolean): ButtonContent = ButtonContent(style, label, emoji, disabled)

    companion object {
        /**
         * Constructs a [ButtonContent] with a label.
         */
        @JvmStatic
        fun fromLabel(style: ButtonStyle, label: String): ButtonContent {
            return ButtonContent(style, label, null, disabled = false)
        }

        /**
         * Constructs a [ButtonContent] with an [Emoji].
         */
        @JvmStatic
        fun fromEmoji(style: ButtonStyle, emoji: Emoji): ButtonContent {
            return ButtonContent(style, null, emoji, disabled = false)
        }

        /**
         * Constructs a [ButtonContent] with a label and an [Emoji].
         */
        @JvmStatic
        fun fromEmoji(style: ButtonStyle, label: String, emoji: Emoji): ButtonContent {
            return ButtonContent(style, label, emoji, disabled = false)
        }

        /**
         * Constructs a [ButtonContent] with a Unicode emoji,
         * see [Emoji.fromUnicode] for accepted formats.
         */
        @JvmStatic
        fun fromUnicode(style: ButtonStyle, unicode: String): ButtonContent {
            return ButtonContent(style, null, Emoji.fromUnicode(unicode), disabled = false)
        }
    }
}
