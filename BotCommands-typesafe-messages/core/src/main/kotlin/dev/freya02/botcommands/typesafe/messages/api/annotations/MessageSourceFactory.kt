package dev.freya02.botcommands.typesafe.messages.api.annotations

import org.springframework.stereotype.Component

@Component
@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
annotation class MessageSourceFactory(val bundleName: String)
