package io.github.freya022.botcommands.api.commands.annotations

import org.jspecify.annotations.Nullable

/**
 * Marks a parameter as being optional (i.e. nullable).
 *
 * **Note**: Kotlin users are not required to use this annotation, using `?` is enough.
 *
 * I recommend using [@Nullable][Nullable] annotation instead, to benefit from nullability analysis.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Optional
