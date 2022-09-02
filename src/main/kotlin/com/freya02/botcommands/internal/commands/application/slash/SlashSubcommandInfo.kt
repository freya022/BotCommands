package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl

open class SlashSubcommandInfo internal constructor(
    context: BContextImpl,
    override val topLevelInstance: TopLevelSlashCommandInfo,
    builder: SlashCommandBuilder
) : SlashCommandInfo(
    context,
    builder
)