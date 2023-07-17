package com.freya02.botcommands.api.commands.prefixed.annotations

/**
 * Specifies the global help description for this command.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Description(val value: String)
