package com.freya02.botcommands.api.commands.application.annotations

import com.freya02.botcommands.api.commands.application.ApplicationCommand;

/**
 * Sets an **unique** command ID on an **annotated** application command function.
 *
 * @see ApplicationCommand.getGuildsForCommandId
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandId(val value: String)
