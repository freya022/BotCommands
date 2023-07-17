package com.freya02.botcommands.api.commands.prefixed.annotations

/**
 * Hides a command from help content and execution.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Hidden  