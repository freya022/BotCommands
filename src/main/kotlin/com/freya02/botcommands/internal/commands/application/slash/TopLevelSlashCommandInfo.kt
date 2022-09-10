package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.mixins.ITopLevelSlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.mixins.TopLevelSlashCommandInfoMixin

class TopLevelSlashCommandInfo internal constructor(
    context: BContextImpl,
    builder: TopLevelSlashCommandBuilder
) : SlashCommandInfo(context, builder),
    ITopLevelSlashCommandInfo by TopLevelSlashCommandInfoMixin(context, builder) {

    override val topLevelInstance: TopLevelSlashCommandInfo = this
    override val parentInstance = null

    val subcommands: Map<String, SlashSubcommandInfo> = builder.subcommands.associate { it.name to it.build(this, this) }
    val subcommandGroups: Map<String, SlashSubcommandGroupInfo> = builder.subcommandGroups.associate { it.name to it.build(this) }

    fun isTopLevelCommandOnly() = subcommands.isEmpty() && subcommandGroups.isEmpty()
}