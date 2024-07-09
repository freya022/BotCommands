package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.config.BConfigBuilder

/**
 * Hides a command and its subcommands from help content and execution,
 * except for [bot owners][BConfigBuilder.predefinedOwnerIds].
 *
 * **Note:** This applies to the command itself, not only this variation,
 * in other words, this applies to all commands with the same path.
 *
 * @see TextCommandBuilder.hidden DSL equivalent
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Hidden  