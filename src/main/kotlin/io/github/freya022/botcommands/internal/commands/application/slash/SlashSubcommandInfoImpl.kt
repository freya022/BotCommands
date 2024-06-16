package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.core.BContext

internal class SlashSubcommandInfoImpl internal constructor(
    context: BContext,
    override val topLevelInstance: TopLevelSlashCommandInfoImpl,
    override val parentInstance: INamedCommand,
    builder: SlashCommandBuilder
) : SlashCommandInfoImpl(context, builder),
    SlashSubcommandInfo