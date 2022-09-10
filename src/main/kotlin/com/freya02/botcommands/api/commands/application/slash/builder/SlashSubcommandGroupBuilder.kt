package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder.Companion.DEFAULT_DESCRIPTION
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandGroupInfo
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo

class SlashSubcommandGroupBuilder(private val context: BContextImpl, val name: String, private val topLevelBuilder: TopLevelSlashCommandBuilder) {
    @get:JvmSynthetic
    internal val subcommands: MutableList<SlashSubcommandBuilder> = mutableListOf()

    var description: String = DEFAULT_DESCRIPTION

    fun subcommand(name: String, block: SlashSubcommandBuilder.() -> Unit) {
        subcommands += SlashSubcommandBuilder(context, name, topLevelBuilder).apply(block)
    }

    fun build(topLevelInstance: TopLevelSlashCommandInfo): SlashSubcommandGroupInfo {
        return SlashSubcommandGroupInfo(topLevelInstance, this)
    }
}
