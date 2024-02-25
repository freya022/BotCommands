package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.declaration.TextCommandManager

/**
 * Marks the function as a text command declaration function.
 *
 * The first argument must be a [TextCommandManager], and is allowed to declare no commands, or multiple commands.
 *
 * **Requirement:** The declaring class must be annotated with [@Command][Command].
 *
 * @see Command @Command
 * @see JDATextCommandVariation @JDATextCommandVariation
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextDeclaration