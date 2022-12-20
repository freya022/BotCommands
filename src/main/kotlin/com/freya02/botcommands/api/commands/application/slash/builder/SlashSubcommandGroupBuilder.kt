package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder.Companion.DEFAULT_DESCRIPTION
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.SimpleCommandMap
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandGroupInfo
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo

class SlashSubcommandGroupBuilder(private val context: BContextImpl, val name: String, private val topLevelBuilder: TopLevelSlashCommandBuilder) {
    @get:JvmSynthetic
    internal val subcommands: SimpleCommandMap<SlashSubcommandBuilder> = SimpleCommandMap.ofBuilders()

    var description: String = DEFAULT_DESCRIPTION

    fun subcommand(name: String, block: SlashSubcommandBuilder.() -> Unit) {
        SlashSubcommandBuilder(context, name, topLevelBuilder).apply(block).also(subcommands::putNewCommand)
    }

    fun build(topLevelInstance: TopLevelSlashCommandInfo): SlashSubcommandGroupInfo {
        return SlashSubcommandGroupInfo(topLevelInstance, this)
    }
}
