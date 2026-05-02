package io.github.freya022.botcommands.api.commands.application

import net.dv8tion.jda.api.entities.Guild

/**
 * Result of a scheduled command update.
 *
 * More details in debug/trace logs.
 */
class CommandUpdateResult internal constructor(
    /**
     * The guild the commands were updated in, `null` for global commands
     */
    val guild: Guild?,
    /**
     * `true` if the commands were updated
     */
    val updatedCommands: Boolean,
    /**
     * The list of failed command declaration functions alongside their exceptions
     */
    val updateExceptions: List<CommandUpdateException>
) {
    override fun toString(): String = buildString {
        if (updatedCommands) {
            append("Updated")
        } else {
            append("Skipped")
        }

        if (guild == null) {
            append(" global")
        } else {
            append(" guild (${guild.id})")
        }
        append(" commands")

        if (updateExceptions.isNotEmpty()) {
            append(", with ${updateExceptions.size} exceptions")
        }
    }
}
