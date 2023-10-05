package io.github.freya022.botcommands.api.commands.prefixed.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command

/**
 * Declares the function as a text command declaration function.
 *
 * **Note:** The function may be called more than once.
 *
 * **Requirement:** The declaring class must be annotated with [@Command][Command].
 *
 * @see Command @Command
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextDeclaration