package io.github.freya022.botcommands.api.commands.text.annotations

/**
 * Specifies the global category of this top-level command.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Category(val value: String)
