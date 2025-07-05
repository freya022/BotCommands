package io.github.freya022.botcommands.api.emojis.annotations

import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji

/**
 * Additional metadata for an application emoji.
 *
 * Only works on [ApplicationEmoji] fields inside [@AppEmojiContainer][AppEmojiContainer] annotated classes.
 *
 * **Kotlin note:** This cannot be used with [AppEmojisRegistry.lazy].
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class AppEmoji(
    /**
     * The name of the asset to load, including the extension.
     *
     * This defaults to the field name, converted to `snake_case`.
     */
    val assetPattern: String = DEFAULT,
    /**
     * The name of the emoji as to be seen on Discord.
     *
     * This defaults to the field name, converted to `snake_case`.
     */
    val emojiName: String = DEFAULT,
) {

    companion object {

        @get:JvmSynthetic
        internal const val DEFAULT = ""
    }
}
