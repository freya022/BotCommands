package com.freya02.botcommands.api.commands.annotations

import net.dv8tion.jda.api.Permission

/**
 * Sets the required user permissions to use this text / application command.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UserPermissions(vararg val value: Permission = [], val append: Boolean = false)
