package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.core.BContextImpl

open class SlashSubcommandInfo internal constructor(
    context: BContextImpl,
    override val topLevelInstance: TopLevelSlashCommandInfo,
    override val parentInstance: INamedCommand,
    builder: SlashCommandBuilder
) : SlashCommandInfo(context, builder)