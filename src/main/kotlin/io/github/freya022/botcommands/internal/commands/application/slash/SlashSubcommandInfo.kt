package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand

open class SlashSubcommandInfo internal constructor(
    context: BContext,
    override val topLevelInstance: TopLevelSlashCommandInfo,
    override val parentInstance: INamedCommand,
    builder: SlashCommandBuilder
) : SlashCommandInfo(context, builder)