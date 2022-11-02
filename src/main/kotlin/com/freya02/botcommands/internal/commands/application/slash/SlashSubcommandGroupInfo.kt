package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import com.freya02.botcommands.internal.commands.mixins.INamedCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommandInfo.Companion.computePath

class SlashSubcommandGroupInfo(topLevelInstance: TopLevelSlashCommandInfo, builder: SlashSubcommandGroupBuilder) : INamedCommandInfo {
    override val parentInstance = topLevelInstance
    override val name = builder.name
    override val path: CommandPath by lazy { computePath() }

    val description = builder.description

    val subcommands: Map<String, SlashSubcommandInfo> = builder.subcommands.associate { it.name to it.build(topLevelInstance, this) }
}
