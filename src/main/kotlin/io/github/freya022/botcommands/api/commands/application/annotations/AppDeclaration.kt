package io.github.freya022.botcommands.api.commands.application.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand

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