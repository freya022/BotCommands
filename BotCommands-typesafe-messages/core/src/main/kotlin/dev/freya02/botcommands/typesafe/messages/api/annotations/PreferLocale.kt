package dev.freya02.botcommands.typesafe.messages.api.annotations

import dev.freya02.botcommands.typesafe.messages.api.LocaleScope

@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class PreferLocale(val scope: LocaleScope)
