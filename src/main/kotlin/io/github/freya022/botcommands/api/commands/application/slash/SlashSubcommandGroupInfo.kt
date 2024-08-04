package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolder

/**
 * Represents a slash subcommand group.
 */
interface SlashSubcommandGroupInfo : INamedCommand, IDeclarationSiteHolder {
    /**
     * The description of this slash command group.
     *
     * May have been set manually or come from a **root** localization bundle.
     */
    val description: String

    /**
     * Subcommands of this top-level slash command, the key is the name of the subcommand.
     */
    val subcommands: Map<String, SlashSubcommandInfo>
}