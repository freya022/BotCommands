package dev.freya02.botcommands.typesafe.messages.api.annotations

/**
 * First parameter can be a [DiscordLocale][net.dv8tion.jda.api.interactions.DiscordLocale] or a [Locale][java.util.Locale],
 * they can be nullable or optional, if it is `null`, then the context-specific locale is used.
 *
 * Parameter names are converted to `camelCase`
 *
 * Must return `String`
 */
@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class LocalizedContent(val templateKey: String)
