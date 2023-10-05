package io.github.freya022.botcommands.api.commands.prefixed.annotations

/**
 * Specifies the global help description for this command.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(val value: String)
