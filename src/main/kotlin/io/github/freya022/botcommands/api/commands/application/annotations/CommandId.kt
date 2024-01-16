package io.github.freya022.botcommands.api.commands.application.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData

/**
 * Sets a **unique** command ID on an **annotated** application command function.
 *
 * **Note:** This only applies to top-level commands, for slash commands,
 * this means the annotation needs to be used alongside [@TopLevelSlashCommandData][TopLevelSlashCommandData].
 *
 * @see ApplicationCommand.getGuildsForCommandId
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandId(val value: String)
