package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.slash.SlashSubcommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfoImpl
import kotlin.reflect.KFunction

class SlashSubcommandBuilder internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>,
    override val topLevelBuilder: ITopLevelApplicationCommandBuilder,
    override val parentInstance: INamedCommand
) : SlashCommandBuilder(context, name, function) {
    override val allowOptions: Boolean = true
    override val allowSubcommands: Boolean = false
    override val allowSubcommandGroups: Boolean = false

    internal fun build(topLevelInstance: TopLevelSlashCommandInfoImpl, parentInstance: INamedCommand): SlashSubcommandInfoImpl {
        return SlashSubcommandInfoImpl(context, topLevelInstance, parentInstance, this)
    }
}
