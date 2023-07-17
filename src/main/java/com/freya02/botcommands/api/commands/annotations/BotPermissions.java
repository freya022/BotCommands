package com.freya02.botcommands.api.commands.annotations

import net.dv8tion.jda.api.Permission;

/**
 * Sets the required bot permissions to use this text / application command.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class BotPermissions(vararg val value: Permission = [], val append: Boolean = false)
