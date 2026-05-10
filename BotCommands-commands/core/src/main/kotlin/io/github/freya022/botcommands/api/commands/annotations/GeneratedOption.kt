package io.github.freya022.botcommands.api.commands.annotations

/**
 * Marks a parameter as being a generated option.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class GeneratedOption
