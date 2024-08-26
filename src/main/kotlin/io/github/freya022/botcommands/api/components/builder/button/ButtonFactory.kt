package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import javax.annotation.CheckReturnValue

/**
 * [Button] factory provided by [Buttons].
 */
class ButtonFactory internal constructor(
    private val componentController: ComponentController,
    private val style: ButtonStyle,
    private val label: String?,
    private val emoji: Emoji?
) {
    init {
        require(label != null || emoji != null) { "A label or an emoji needs to be set" }

        if (label != null) {
            require(label.isNotEmpty()) {
                "The label cannot be empty"
            }
        }
    }

    /**
     * Creates a new button factory with the provided emoji alias / emoji unicode.
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
    fun withEmoji(aliasOrUnicode: String?): ButtonFactory {
        val newEmoji = aliasOrUnicode?.let {
            EmojiUtils.resolveJDAEmojiOrNull(it) ?: Emoji.fromFormatted(it)
        }

        return ButtonFactory(componentController, style, label, newEmoji)
    }

    /**
     * Creates an ephemeral button builder.
     *
     * As a reminder, a [default timeout][Components.defaultTimeout] is set by default.
     *
     * @see Components
     */
    @CheckReturnValue
    fun ephemeral(): EphemeralButtonBuilder =
        EphemeralButtonBuilder(componentController, style, label, emoji, InstanceRetriever())

    /**
     * Creates an ephemeral button.
     *
     * As a reminder, a [default timeout][Components.defaultTimeout] is set by default.
     *
     * @see Components
     */
    @JvmSynthetic
    suspend inline fun ephemeral(block: EphemeralButtonBuilder.() -> Unit): Button =
        ephemeral().apply(block).buildSuspend()

    /**
     * Creates a persistent button builder.
     *
     * As a reminder, **no timeout** is set by default.
     *
     * @see Components
     */
    @CheckReturnValue
    fun persistent(): PersistentButtonBuilder =
        PersistentButtonBuilder(componentController, style, label, emoji, InstanceRetriever())

    /**
     * Creates a persistent button.
     *
     * As a reminder, **no timeout** is set by default.
     *
     * @see Components
     */
    @JvmSynthetic
    suspend inline fun persistent(block: PersistentButtonBuilder.() -> Unit): Button =
        persistent().apply(block).buildSuspend()
}