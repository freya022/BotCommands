package io.github.freya022.botcommands.api.commands.text

/**
 * Represents a top-level text command.
 */
interface TopLevelTextCommandInfo : TextCommandInfo {
    /**
     * Category of this text command, only used in the built-in help command.
     */
    val category: String
}