package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.mixins.INamedCommand

open class SlashSubcommandInfo internal constructor(
    context: BContextImpl,
    override val topLevelInstance: TopLevelSlashCommandInfo,
    override val parentInstance: INamedCommand,
    builder: SlashCommandBuilder
) : SlashCommandInfo(context, builder)