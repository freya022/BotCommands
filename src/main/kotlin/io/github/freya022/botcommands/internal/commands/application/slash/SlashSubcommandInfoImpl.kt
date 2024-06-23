package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.core.BContext

internal class SlashSubcommandInfoImpl internal constructor(
    context: BContext,
    topLevelInstance: TopLevelSlashCommandInfoImpl,
    parentInstance: INamedCommand,
    builder: SlashCommandBuilder
) : SlashCommandInfoImpl(context, topLevelInstance, parentInstance, builder),
    SlashSubcommandInfo