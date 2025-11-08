package dev.freya02.botcommands.typesafe.messages.api.annotations

/**
 * Parameter name is converted to `camelCase`
 *
 * Must return `String`
 */
@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class LocalizedContent(val templateKey: String)
