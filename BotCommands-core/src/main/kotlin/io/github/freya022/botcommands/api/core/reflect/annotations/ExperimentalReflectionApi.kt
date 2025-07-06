package io.github.freya022.botcommands.api.core.reflect.annotations

/**
 * Opt-in marker annotation for reflection APIs.
 *
 * The provided APIs have no guarantees and may change (including removals) at any time.
 *
 * Please create an issue or join the Discord server if you encounter a problem or want to submit feedback.
 */
@RequiresOptIn(
    message = "This feature is experimental, please see the documentation of this opt-in annotation (@ExperimentalReflectionApi) for more details.",
    level = RequiresOptIn.Level.ERROR
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class ExperimentalReflectionApi
