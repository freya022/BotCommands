package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.builder.button.ButtonFactory
import io.github.freya022.botcommands.api.components.utils.ButtonContent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import javax.annotation.CheckReturnValue

/**
 * @see Components
 * @see SelectMenus
 */
@BService
@Dependencies(Components::class)
class Buttons internal constructor(componentController: ComponentController) : AbstractComponentFactory(componentController) {
    /**
     * Creates a button factory with the style and label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun of(style: ButtonStyle, label: String): ButtonFactory =
        ButtonFactory(componentController, style, label, null)

    /**
     * Creates a button factory with the style and emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun of(style: ButtonStyle, emoji: Emoji): ButtonFactory =
        ButtonFactory(componentController, style, null, emoji)

    /**
     * Creates a button factory with the style, label and emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun of(style: ButtonStyle, label: String, emoji: Emoji): ButtonFactory =
        ButtonFactory(componentController, style, label, emoji)

    /**
     * Creates a button factory with the style, label and emoji provided by the [ButtonContent].
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is null/blank and the emoji isn't set
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonContent.withEmoji
     */
    @CheckReturnValue
    fun of(content: ButtonContent): ButtonFactory =
        ButtonFactory(componentController, content.style, content.label, content.emoji)

    /**
     * Creates a primary button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun primary(label: String): ButtonFactory =
        of(ButtonStyle.PRIMARY, label)

    /**
     * Creates a primary button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun primary(emoji: Emoji): ButtonFactory =
        of(ButtonStyle.PRIMARY, emoji)

    /**
     * Creates a primary button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun primary(label: String, emoji: Emoji): ButtonFactory =
        of(ButtonStyle.PRIMARY, label, emoji)

    /**
     * Creates a secondary button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun secondary(label: String): ButtonFactory =
        of(ButtonStyle.SECONDARY, label)

    /**
     * Creates a secondary button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun secondary(emoji: Emoji): ButtonFactory =
        of(ButtonStyle.SECONDARY, emoji)

    /**
     * Creates a secondary button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun secondary(label: String, emoji: Emoji): ButtonFactory =
        of(ButtonStyle.SECONDARY, label, emoji)

    /**
     * Creates a success button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun success(label: String): ButtonFactory =
        of(ButtonStyle.SUCCESS, label)

    /**
     * Creates a success button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun success(emoji: Emoji): ButtonFactory =
        of(ButtonStyle.SUCCESS, emoji)

    /**
     * Creates a success button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun success(label: String, emoji: Emoji): ButtonFactory =
        of(ButtonStyle.SUCCESS, label, emoji)

    /**
     * Creates a danger button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun danger(label: String): ButtonFactory =
        of(ButtonStyle.DANGER, label)

    /**
     * Creates a danger button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun danger(emoji: Emoji): ButtonFactory =
        of(ButtonStyle.DANGER, emoji)

    /**
     * Creates a danger button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun danger(label: String, emoji: Emoji): ButtonFactory =
        of(ButtonStyle.DANGER, label, emoji)

    /**
     * Creates a danger button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the url/label is empty
     */
    @CheckReturnValue
    fun link(url: String, label: String): Button =
        Button.link(url, label)

    /**
     * Creates a danger button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the url is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     */
    @CheckReturnValue
    fun link(url: String, emoji: Emoji): Button =
        Button.link(url, emoji)

    /**
     * Creates a danger button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the url/label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     */
    @CheckReturnValue
    fun link(url: String, label: String, emoji: Emoji): Button =
        Button.link(url, label).withEmoji(emoji)
}