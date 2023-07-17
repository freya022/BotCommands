package com.freya02.botcommands.api.commands.annotations

/**
 * Marks this text command as only usable by the bot owners.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class RequireOwner  