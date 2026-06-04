package io.github.freya022.botcommands.api.commands.annotations

import net.dv8tion.jda.api.Permission

/**
 * Sets the required user permissions to use this text / application command.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class UserPermissions(@get:JvmName("value") vararg val permissions: Permission)
