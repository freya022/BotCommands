package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.BotOwners

/**
 * Hides a command and its subcommands from help content and execution,
 * except for [bot owners][BotOwners].
 *
 * ### Annotation scope
 * This annotation applies to the command itself, not just on a single variation,
 * in other words, it applies to all commands sharing the same path.
 *
 * @see TextCommandBuilder.hidden DSL equivalent
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Hidden  
