package io.github.freya022.botcommands.internal.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.slash.SlashSubcommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfoImpl
import kotlin.reflect.KFunction

internal class SlashSubcommandBuilderImpl internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>,
    override val topLevelBuilder: TopLevelApplicationCommandBuilder<SlashCommandOptionAggregateBuilder>,
    override val parentInstance: INamedCommand
) : SlashCommandBuilderImpl(context, name, function),
    SlashSubcommandBuilder {

    override val allowOptions: Boolean = true
    override val allowSubcommands: Boolean = false
    override val allowSubcommandGroups: Boolean = false

    internal fun build(topLevelInstance: TopLevelSlashCommandInfoImpl, parentInstance: INamedCommand): SlashSubcommandInfoImpl {
        return SlashSubcommandInfoImpl(context, topLevelInstance, parentInstance, this)
    }
}