package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder

/**
 * Marks this text command as only usable by the bot owners.
 *
 * **Note:** This applies to the command itself, not only this variation,
 * in other words, this applies to all commands with the same path.
 *
 * @see TextCommandBuilder.ownerRequired DSL equivalent
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireOwner