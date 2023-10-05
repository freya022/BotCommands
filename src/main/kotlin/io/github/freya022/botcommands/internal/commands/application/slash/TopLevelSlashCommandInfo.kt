package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.internal.commands.application.slash.mixins.ITopLevelSlashCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.mixins.TopLevelSlashCommandInfoMixin
import io.github.freya022.botcommands.internal.core.BContextImpl

class TopLevelSlashCommandInfo internal constructor(
    context: BContextImpl,
    builder: TopLevelSlashCommandBuilder
) : SlashCommandInfo(context, builder),
    ITopLevelSlashCommandInfo by TopLevelSlashCommandInfoMixin(builder) {

    override val topLevelInstance: TopLevelSlashCommandInfo = this
    override val parentInstance = null

    val subcommands: Map<String, SlashSubcommandInfo> = builder.subcommands.map.mapValues { it.value.build(this, this) }
    val subcommandGroups: Map<String, SlashSubcommandGroupInfo> = builder.subcommandGroups.map.mapValues { it.value.build(this) }

    fun isTopLevelCommandOnly() = subcommands.isEmpty() && subcommandGroups.isEmpty()
}