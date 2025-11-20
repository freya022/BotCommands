package dev.freya02.botcommands.typesafe.messages.api.annotations

import dev.freya02.botcommands.typesafe.messages.api.LocalePreference

/**
 * Allows changing the default locale with another preferred locale,
 * the available locales will depend on how the message source was created.
 *
 * When applied to an interface, all functions are affected,
 * if the annotation is on both the function and the class, the function is prioritized.
 *
 * @see preference
 */
@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class PreferLocale(
    /**
     * The locale to prefer.
     */
    @get:JvmName("value")
    val preference: LocalePreference
)
