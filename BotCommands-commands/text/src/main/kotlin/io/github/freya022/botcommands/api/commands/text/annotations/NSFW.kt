package io.github.freya022.botcommands.api.commands.text.annotations

/**
 * Marks a text command as being usable in NSFW channels only.
 *
 * ### Annotation scope
 * This annotation applies to the command itself, not just on a single variation,
 * in other words, it applies to all commands sharing the same path.
 *
 * ### Built-in help content
 * NSFW commands will be shown if requested in an NSFW channel.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NSFW
