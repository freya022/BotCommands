package io.github.freya022.botcommands.api.emojis.annotations

import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji

/**
 * Declares this class as an [ApplicationEmoji] container.
 *
 * Each annotated class will be introspected and have all of their [ApplicationEmoji]s registered.
 * When all emojis are registered, they will be retrieved/created as needed before JDA connects to the gateway,
 * thus all emojis will be ready once the bot starts up.
 *
 * ### Kotlin and Java usage
 * You can create `ApplicationEmoji` fields which uses [AppEmojisRegistry] to get an application emoji,
 * in Kotlin you can delegate with `by AppEmojisRegistry`, for example:
 * ```kt
 * // Gets emojis from /emojis by default
 * @AppEmojiContainer
 * object AppEmojis {
 *     val myEmoji by AppEmojisRegistry
 * }
 * ```
 *
 * While in Java, you can use [`AppEmojisRegistry.get("myFieldName")`][AppEmojisRegistry.get], for example:
 * ```java
 * // Gets emojis from /emojis by default
 * @AppEmojiContainer
 * public class AppEmojis {
 *     // Give it the name of the field, it will return the emoji
 *     // The "SCREAMING_SNAKE_CASE" naming scheme is also supported
 *     public static final ApplicationEmoji myEmoji = AppEmojisRegistry.get("myEmoji")
 * }
 * ```
 *
 * The loader will load each [ApplicationEmoji] field/property based on the base path set in [@AppEmojiContainer][AppEmojiContainer],
 * while the asset name is the field name, with a `camelCase` format,
 * in the examples above, it would load emojis in `emojis/my_emoji` with *any* extension,
 * failing if multiple extensions are found.
 *
 * You can optionally customize each field with [@AppEmoji][AppEmoji].
 *
 * ### Lazy usage (Kotlin only)
 * You can create delegated properties that use [AppEmojisRegistry.lazy] to benefit from more customization, for example:
 * ```kt
 * // Gets emojis from /emojis by default
 * @AppEmojiContainer
 * object AppEmojis {
 *     // Create an emoji named 'not_my_emoji' from the file at 'emojis/not_my_emoji.png'
 *     // you can also customize the 'basePath', which overrides the one set in @AppEmojiContainer
 *     val myEmoji: ApplicationEmoji by AppEmojisRegistry.lazy(::myEmoji, /* Optional overrides */ assetPattern = "not_my_emoji.png", emojiName = "not_my_emoji")
 * }
 * ```
 *
 * See [AppEmojisRegistry] for more details on the loading process.
 *
 * @see AppEmojisRegistry
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AppEmojiContainer(
    /**
     * Prefixes the path used to load the application emojis,
     * must start at the resource root (start with `/`),
     * for example, `/app_emojis`.
     */
    val basePath: String = "/emojis",
)
