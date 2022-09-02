package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder

class SlashSubcommandGroupInfo(topLevelInstance: TopLevelSlashCommandInfo, builder: SlashSubcommandGroupBuilder) {
    val name = builder.name
    val description = builder.description

    val subcommands: Map<String, SlashSubcommandInfo> = builder.subcommands.associate { it.name to it.build(topLevelInstance) }
}
