package dev.freya02.botcommands.jda.keepalive.api

/**
 * Opt-in marker annotation for the JDA keep alive feature.
 *
 * This feature provides no guarantee and its API may change (including removals) at any time.
 *
 * Please create an issue if you encounter a problem, including if it needs adaptations for your use case.
 */
@RequiresOptIn(
    message = "This feature is experimental, please see the documentation of this opt-in annotation (@ExperimentalKeepAliveApi) for more details.",
    level = RequiresOptIn.Level.ERROR
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class ExperimentalKeepAliveApi
