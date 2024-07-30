package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl

internal class SlashSubcommandInfoImpl internal constructor(
    context: BContext,
    topLevelInstance: TopLevelSlashCommandInfoImpl,
    parentInstance: INamedCommand,
    builder: SlashCommandBuilderImpl
) : SlashCommandInfoImpl(context, topLevelInstance, parentInstance, builder),
    SlashSubcommandInfo