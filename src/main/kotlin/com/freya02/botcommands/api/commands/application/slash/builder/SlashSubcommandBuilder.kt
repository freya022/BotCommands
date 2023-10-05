package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandInfo
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.core.BContextImpl
import kotlin.reflect.KFunction

class SlashSubcommandBuilder internal constructor(
    context: BContextImpl,
    name: String,
    function: KFunction<Any>,
    override val topLevelBuilder: ITopLevelApplicationCommandBuilder,
    override val parentInstance: INamedCommand
) : SlashCommandBuilder(context, name, function) {
    override val allowOptions: Boolean = true
    override val allowSubcommands: Boolean = false
    override val allowSubcommandGroups: Boolean = false

    internal fun build(topLevelInstance: TopLevelSlashCommandInfo, parentInstance: INamedCommand): SlashSubcommandInfo {
        return SlashSubcommandInfo(context, topLevelInstance, parentInstance, this)
    }
}
