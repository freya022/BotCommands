package io.github.freya022.botcommands.api.commands.annotations

/**
 * Marks an app/text command as being usable in NSFW channels only.
 *
 * ### Built-in help content
 * NSFW commands will be shown if requested in an NSFW channel.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NSFW
