package dev.freya02.botcommands.typesafe.messages.api.annotations

import dev.freya02.botcommands.typesafe.messages.api.LocaleScope

// TODO also enable for entire class
@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class PreferLocale(val scope: LocaleScope)
