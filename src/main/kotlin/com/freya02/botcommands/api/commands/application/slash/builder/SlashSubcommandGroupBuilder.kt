package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder.Companion.DEFAULT_DESCRIPTION
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.SimpleCommandMap
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandGroupInfo
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import net.dv8tion.jda.internal.utils.Checks

class SlashSubcommandGroupBuilder(private val context: BContextImpl, override val name: String, private val topLevelBuilder: TopLevelSlashCommandBuilder) : INamedCommand {
    override val parentInstance: INamedCommand = topLevelBuilder
    override val path: CommandPath by lazy { computePath() }

    @get:JvmSynthetic
    internal val subcommands: SimpleCommandMap<SlashSubcommandBuilder> = SimpleCommandMap.ofBuilders()

    var description: String = DEFAULT_DESCRIPTION

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

    fun subcommand(name: String, block: SlashSubcommandBuilder.() -> Unit) {
        SlashSubcommandBuilder(context, name, topLevelBuilder, this).apply(block).also(subcommands::putNewCommand)
    }

    fun build(topLevelInstance: TopLevelSlashCommandInfo): SlashSubcommandGroupInfo {
        return SlashSubcommandGroupInfo(topLevelInstance, this)
    }
}
