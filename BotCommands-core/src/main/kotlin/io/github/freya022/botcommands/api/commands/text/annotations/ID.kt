package io.github.freya022.botcommands.api.commands.text.annotations

/**
 * Adapts the help content of this parameter as an ID of an entity (message, user, guild, channel, role...)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ID
