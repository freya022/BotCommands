package io.github.freya022.botcommands.api.commands.application.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand

/**
 * Sets an **unique** command ID on an **annotated** application command function.
 *
 * @see ApplicationCommand.getGuildsForCommandId
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandId(val value: String)
