package io.github.freya022.botcommands.api.commands.text.builder

interface TopLevelTextCommandBuilder : TextCommandBuilder {

    /**
     * The category of this top-level command.
     *
     * This is solely used by the built-in help command. All subcommands will share this.
     */
    var category: String
}
