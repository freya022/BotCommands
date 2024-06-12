package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandMetadata
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.commands.application.TopLevelApplicationCommandMetadataAccessor
import io.github.freya022.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin

internal class TopLevelSlashCommandInfoImpl internal constructor(
    context: BContext,
    builder: TopLevelSlashCommandBuilder
) : SlashCommandInfoImpl(context, builder),
    TopLevelApplicationCommandInfo by TopLevelApplicationCommandInfoMixin(builder),
    TopLevelSlashCommandInfo,
    TopLevelApplicationCommandMetadataAccessor {

    override val topLevelInstance: TopLevelSlashCommandInfoImpl get() = this
    override val parentInstance get() = null

    override lateinit var metadata: TopLevelApplicationCommandMetadata

    override val subcommands: Map<String, SlashSubcommandInfoImpl> =
        builder.subcommands.map
            .mapValues { it.value.build(this, this) }
            .unmodifiableView()
    override val subcommandGroups: Map<String, SlashSubcommandGroupInfoImpl> =
        builder.subcommandGroups.map
            .mapValues { it.value.build(this) }
            .unmodifiableView()

    override val isTopLevelCommandOnly get() = subcommands.isEmpty() && subcommandGroups.isEmpty()
}