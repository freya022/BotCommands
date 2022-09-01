package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelSlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.TopLevelSlashCommandBuilderMixin
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo

class TopLevelSlashCommandBuilder internal constructor(
    context: BContextImpl,
    name: String,
    scope: CommandScope
) : SlashCommandBuilder(context, name), ITopLevelSlashCommandBuilder by TopLevelSlashCommandBuilderMixin(scope) {
    @get:JvmSynthetic
    internal val subcommands: MutableList<SlashCommandBuilder> = mutableListOf()
    @get:JvmSynthetic
    internal val subcommandGroups: MutableList<SlashSubcommandGroupBuilder> = mutableListOf()

    fun subcommand(name: String, block: SlashSubcommandBuilder.() -> Unit) {
        subcommands += SlashSubcommandBuilder(context, name).apply(block)
    }

    fun subcommandGroup(name: String, block: SlashSubcommandGroupBuilder.() -> Unit) {
        subcommandGroups += SlashSubcommandGroupBuilder(context, name).apply(block)
    }

    internal fun build(): TopLevelSlashCommandInfo {
        checkFunction()
        return TopLevelSlashCommandInfo(context, this)
    }
}
