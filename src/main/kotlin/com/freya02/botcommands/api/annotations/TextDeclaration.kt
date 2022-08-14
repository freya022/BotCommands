package com.freya02.botcommands.api.annotations

/**
 * Marks a function as one which declares text commands
 *
 * **The function may be called more than once**
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class TextDeclaration