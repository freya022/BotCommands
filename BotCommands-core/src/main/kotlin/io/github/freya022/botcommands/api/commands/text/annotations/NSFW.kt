package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder

/**
 * Marks a text command as being usable in NSFW channels only.
 *
 * ### Built-in help content
 * NSFW commands will be shown if requested in an NSFW channel.
 *
 * @see TextCommandBuilder.nsfw DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NSFW