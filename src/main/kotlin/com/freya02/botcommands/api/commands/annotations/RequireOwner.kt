package com.freya02.botcommands.api.commands.annotations

/**
 * Marks this text command as only usable by the bot owners.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireOwner