package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandMetadata
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.commands.application.mixins.TopLevelApplicationCommandInfoMixin
import io.github.freya022.botcommands.internal.commands.application.slash.builder.TopLevelSlashCommandBuilderImpl

internal class TopLevelSlashCommandInfoImpl internal constructor(
    context: BContext,
    builder: TopLevelSlashCommandBuilderImpl
) : SlashCommandInfoImpl(context, null, null, builder),
    TopLevelSlashCommandInfo,
    TopLevelApplicationCommandInfoMixin {

    override val topLevelInstance: TopLevelSlashCommandInfoImpl get() = this
    override val parentInstance get() = null

    override lateinit var metadata: TopLevelApplicationCommandMetadata

    override val scope: CommandScope = builder.scope
    override val isDefaultLocked: Boolean = builder.isDefaultLocked
    override val isGuildOnly: Boolean = scope.isGuildOnly
    override val nsfw: Boolean = builder.nsfw

    override val subcommands: Map<String, SlashSubcommandInfoImpl> =
        builder.subcommands.map
            .mapValues { it.value.build(this, this) }
            .unmodifiableView()
    override val subcommandGroups: Map<String, SlashSubcommandGroupInfoImpl> =
        builder.subcommandGroups.map
            .mapValues { it.value.build(this) }
            .unmodifiableView()

    init {
        initChecks(builder)
    }
}
