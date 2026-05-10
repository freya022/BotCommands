package io.github.freya022.botcommands.api.commands

/**
 * A command with a name and possibly a parent.
 */
interface INamedCommand {
    /**
     * The parent of this command, `null` for top-level commands.
     */
    val parentInstance: INamedCommand?

    /**
     * The name of this command.
     *
     * Does not include the names of its parents.
     */
    val name: String

    /**
     * The complete path that leads to this command.
     */
    val path: CommandPath
}