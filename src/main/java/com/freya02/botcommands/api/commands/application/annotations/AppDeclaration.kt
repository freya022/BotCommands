package com.freya02.botcommands.api.commands.application.annotations

import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager

/**
 * Marks a function as one which declares commands, you can make your application commands in this function
 *
 * The first argument needs to be a [GuildApplicationCommandManager] or a [GlobalApplicationCommandManager]
 *
 * **The function may be called more than once**, for example, if the bot needs to update its commands, or if it joins a guild
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AppDeclaration //TODO rename to AppDeclaration, update docs