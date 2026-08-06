package io.github.freya022.botcommands.api.core.hooks.custom.annotations

import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.TYPEALIAS
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * Opt-in marker annotation for APIs of custom events that are considered experimental and are not subject to compatibility guarantees:
 * The behavior of such API may be changed or the API may be removed completely in any further release.
 *
 * Please create an issue or join the Discord server if you encounter a problem or want to submit feedback.
 *
 * Any usage of a declaration annotated with `@ExperimentalCustomEvents` must be accepted either by
 * annotating that usage with the [@OptIn][OptIn] annotation, e.g. `@OptIn(ExperimentalCustomEvents::class)`,
 * or by using the compiler argument `-opt-in=io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents`.
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
annotation class ExperimentalCustomEvents
