package com.freya02.botcommands.api.commands.application.annotations

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand

/**
 * Annotation for functions which declares application commands.
 *
 * The first argument needs to be a [GuildApplicationCommandManager] or a [GlobalApplicationCommandManager].
 *
 * **Note:** The function may declare no command,
 * and may be called more than once, for example,
 * if the bot needs to update its commands, or if it joins a guild.
 *
 * **Requirement:** The declaring class must be annotated with [@Command][Command].
 *
 * @see Command @Command
 * @see JDASlashCommand @JDASlashCommand
 * @see JDAMessageCommand @JDAMessageCommand
 * @see JDAUserCommand @JDAUserCommand
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AppDeclaration