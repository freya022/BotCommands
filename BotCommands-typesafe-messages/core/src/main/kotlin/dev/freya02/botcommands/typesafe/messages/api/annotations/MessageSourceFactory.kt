package dev.freya02.botcommands.typesafe.messages.api.annotations

@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
annotation class MessageSourceFactory(val bundleName: String)
