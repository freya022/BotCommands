package com.freya02.botcommands.api.commands.prefixed.annotations

/**
 * Specifies the global category of this top-level command.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Category(val value: String)
