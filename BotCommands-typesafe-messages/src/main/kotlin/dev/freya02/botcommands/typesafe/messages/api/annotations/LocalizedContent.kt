package dev.freya02.botcommands.typesafe.messages.api.annotations

@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class LocalizedContent(val templateKey: String)
