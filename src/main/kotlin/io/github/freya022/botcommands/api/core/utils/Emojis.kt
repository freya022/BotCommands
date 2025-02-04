package io.github.freya022.botcommands.api.core.utils

import dev.freya02.jda.emojis.unicode.Emojis
import dev.freya02.jda.emojis.unicode.UnicodeEmojis
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.fellbaum.jemoji.Emoji as JEmoji

/**
 * Converts this JEmoji [Emoji][JEmoji] into a JDA [UnicodeEmoji].
 *
 * **Note:** If you use the emoji constants, you can instead use the constants from [Emojis] or [UnicodeEmojis].
 *
 * @see lazyUnicodeEmoji
 */
fun JEmoji.asUnicodeEmoji(): UnicodeEmoji = Emoji.fromUnicode(emoji)

/**
 * Lazily converts the supplied [Emoji][JEmoji] into a JDA [UnicodeEmoji].
 *
 * **Note:** If you use the emoji constants, you can instead use the constants from [Emojis] or [UnicodeEmojis].
 *
 * This is useful to load JEmoji only when it's necessary, avoiding any startup delay.
 *
 * An alternative is to use the emoji in-place, i.e., where is it actually used.
 */
@JvmName("lazyJEmojiUnicodeEmoji")
fun lazyUnicodeEmoji(supplier: () -> JEmoji): Lazy<UnicodeEmoji> =
    lazy { supplier().asUnicodeEmoji() }

/**
 * Lazily converts the supplied [Emoji] into a JDA [UnicodeEmoji].
 *
 * This is useful to load emojis only when it's necessary, avoiding any startup delay.
 *
 * An alternative is to use the emoji in-place, i.e., where is it actually used.
 */
@JvmName("lazyJdaUnicodeEmoji")
fun lazyUnicodeEmoji(supplier: () -> UnicodeEmoji): Lazy<UnicodeEmoji> =
    lazy { supplier() }