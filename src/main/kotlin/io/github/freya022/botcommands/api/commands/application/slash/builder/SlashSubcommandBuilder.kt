package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.internal.commands.application.slash.SlashSubcommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.core.BContextImpl
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
