package dev.freya02.botcommands.restarter.api

import kotlin.annotation.AnnotationTarget.*

/**
 * Opt-in marker annotation for "hot restart" APIs that are considered experimental and are not subject to compatibility guarantees:
 * The behavior of such API may be changed or the API may be removed completely in any further release.
 *
 * Please create an issue or join the Discord server if you encounter a problem or want to submit feedback.
 *
 * Any usage of a declaration annotated with `@ExperimentalRestartApi` must be accepted either by
 * annotating that usage with the [@OptIn][OptIn] annotation, e.g. `@OptIn(ExperimentalRestartApi::class)`,
 * or by using the compiler argument `-opt-in=dev.freya02.botcommands.restarter.api.ExperimentalRestartApi`.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
    CLASS,
    ANNOTATION_CLASS,
    PROPERTY,
    FIELD,
    LOCAL_VARIABLE,
    VALUE_PARAMETER,
    CONSTRUCTOR,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    TYPEALIAS
)
@MustBeDocumented
annotation class ExperimentalRestartApi
