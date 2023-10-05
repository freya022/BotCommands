package io.github.freya022.botcommands.api.commands.annotations

import net.dv8tion.jda.api.Permission

/**
 * Sets the required bot permissions to use this text / application command.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BotPermissions(vararg val value: Permission = [], val append: Boolean = false)
