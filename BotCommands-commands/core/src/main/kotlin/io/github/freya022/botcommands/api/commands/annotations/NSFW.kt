package io.github.freya022.botcommands.api.commands.annotations

/**
 * Marks a text command as being usable in NSFW channels only.
 *
 * **Note:** This applies to the command itself, not only this variation,
 * in other words, this applies to all commands with the same path.
 *
 * ### Built-in help content
 * NSFW commands will be shown if requested in an NSFW channel.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NSFW
