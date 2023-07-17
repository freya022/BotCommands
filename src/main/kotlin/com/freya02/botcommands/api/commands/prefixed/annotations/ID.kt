package com.freya02.botcommands.api.commands.prefixed.annotations

/**
 * Adapts the help content of this parameter as an ID of an entity (message, user, guild, channel, role...)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ID
