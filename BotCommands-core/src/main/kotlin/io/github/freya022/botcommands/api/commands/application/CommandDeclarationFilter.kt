package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.entities.Guild

/**
 * A filter determining if an application command needs to be registered.
 *
 * Needs to be referenced by a [@DeclarationFilter][DeclarationFilter].
 *
 * **Usage**: Register your instance as a service with [@BService][BService].
 *
 * @see DeclarationFilter @DeclarationFilter
 */
interface CommandDeclarationFilter {
    /**
     * Returns whether the provided command can be used in the provided guild.
     *
     * @param guild     The guild the command is going to be registered at
     * @param path      The complete path of the command
     * @param commandId The command ID set by [@CommandId][CommandId]
     */
    fun filter(guild: Guild, path: CommandPath, commandId: String?): Boolean
}