package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder

interface SlashSubcommandGroupInfo : INamedCommand, IDeclarationSiteHolder {
    val description: String

    val subcommands: Map<String, SlashSubcommandInfo>
}