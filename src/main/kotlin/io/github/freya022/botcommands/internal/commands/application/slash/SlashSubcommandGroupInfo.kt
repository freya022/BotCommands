package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import io.github.freya022.botcommands.internal.utils.LocalizationUtils

class SlashSubcommandGroupInfo(topLevelInstance: TopLevelSlashCommandInfo, builder: SlashSubcommandGroupBuilder) : INamedCommand {
    override val parentInstance = topLevelInstance
    override val name = builder.name
    override val path: CommandPath by lazy { computePath() }

    val description = LocalizationUtils.getCommandDescription(topLevelInstance.context, builder, builder.description)

    val subcommands: Map<String, SlashSubcommandInfo> = builder.subcommands.map.mapValues { it.value.build(topLevelInstance, this) }
}
