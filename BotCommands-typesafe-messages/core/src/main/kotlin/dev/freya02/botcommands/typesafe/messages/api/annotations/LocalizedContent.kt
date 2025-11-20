package dev.freya02.botcommands.typesafe.messages.api.annotations

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

/**
 * Annotates a method as a message supplier.
 *
 * ### Requirements
 * - Return a [String]
 * - Be non-suspending
 * - Have no optional parameters
 * - Be in an interface extending [IMessageSource]
 *
 * ### Optional annotations
 * - [@PreferLocale][PreferLocale]: Allows changing the default locale with another preferred locale
 *
 * ### Parameters
 * - The first parameter can be a [DiscordLocale] or a [Locale], they can be nullable, if it is `null`, then the default locale is used
 * - Each following parameter is a template argument
 *    - Their name is converted from `camelCase` to `snake_case`
 *    - Their value is converted to a string using [toString]
 *
 * @see templateKey
 */
@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class LocalizedContent(
    /**
     * The key to the localizable template, in the bundle specified by [@MessageSourceFactory][MessageSourceFactory].
     */
    @get:JvmName("value")
    val templateKey: String
)
