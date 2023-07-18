package com.freya02.botcommands.api.commands.annotations

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder

/**
 * Marks this text command as only usable by the bot owners.
 *
 * @see TextCommandBuilder.ownerRequired DSL equivalent
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireOwner