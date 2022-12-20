package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandInfo
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommand

class SlashSubcommandBuilder internal constructor(
    context: BContextImpl,
    name: String,
    override val topLevelBuilder: ITopLevelApplicationCommandBuilder,
    override val parentInstance: INamedCommand
) : SlashCommandBuilder(context, name) {
    override val allowOptions: Boolean = true
    override val allowSubcommands: Boolean = false
    override val allowSubcommandGroups: Boolean = false

    internal fun build(topLevelInstance: TopLevelSlashCommandInfo, parentInstance: INamedCommand): SlashSubcommandInfo {
        checkFunction()
        return SlashSubcommandInfo(context, topLevelInstance, parentInstance, this)
    }
}
