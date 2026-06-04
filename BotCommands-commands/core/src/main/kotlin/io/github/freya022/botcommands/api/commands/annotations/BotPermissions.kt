package io.github.freya022.botcommands.api.commands.annotations

import net.dv8tion.jda.api.Permission

/**
 * Sets the required bot permissions to use this text / application command.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BotPermissions(@get:JvmName("value") vararg val permissions: Permission)
