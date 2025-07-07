package dev.freya02.botcommands.typesafe.messages.api.annotations

/**
 * Opt-in marker annotation for the type-safe (localized) messages feature.
 *
 * The provided APIs have no guarantees and may change (including removals) at any time.
 *
 * Please create an issue or join the Discord server if you encounter a problem or want to submit feedback.
 */
@RequiresOptIn(
    message = "This feature is experimental, please see the documentation of this opt-in annotation (@ExperimentalTypesafeMessagesApi) for more details.",
    level = RequiresOptIn.Level.ERROR
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class ExperimentalTypesafeMessagesApi
