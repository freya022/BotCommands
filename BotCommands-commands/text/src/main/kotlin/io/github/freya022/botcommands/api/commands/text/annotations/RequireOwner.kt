package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.BotOwners

/**
 * Marks this text command as only usable by the [bot owners][BotOwners].
 *
 * Owner-only commands are hidden in the built-in help content,
 * but will still be responded to if a user tries to use it,
 * though they will be rejected.
 *
 * ### Annotation scope
 * This annotation applies to the command itself, not just on a single variation,
 * in other words, it applies to all commands sharing the same path.
 *
 * @see TextCommandBuilder.ownerRequired DSL equivalent
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireOwner
