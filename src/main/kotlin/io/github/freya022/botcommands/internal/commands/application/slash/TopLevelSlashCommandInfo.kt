package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.commands.application.slash.mixins.ITopLevelSlashCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.mixins.TopLevelSlashCommandInfoMixin

class TopLevelSlashCommandInfo internal constructor(
    context: BContext,
    builder: TopLevelSlashCommandBuilder
) : SlashCommandInfo(context, builder),
    ITopLevelSlashCommandInfo by TopLevelSlashCommandInfoMixin(builder) {

    override val topLevelInstance: TopLevelSlashCommandInfo get() = this
    override val parentInstance get() = null

    val subcommands: Map<String, SlashSubcommandInfo> = builder.subcommands.map.mapValues { it.value.build(this, this) }.unmodifiableView()
    val subcommandGroups: Map<String, SlashSubcommandGroupInfo> = builder.subcommandGroups.map.mapValues { it.value.build(this) }.unmodifiableView()

    fun isTopLevelCommandOnly() = subcommands.isEmpty() && subcommandGroups.isEmpty()
}