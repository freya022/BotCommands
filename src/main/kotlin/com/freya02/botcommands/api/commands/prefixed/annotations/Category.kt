package com.freya02.botcommands.api.commands.prefixed.annotations

/**
 * Specifies the global category of this top-level command.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Category(val value: String)
