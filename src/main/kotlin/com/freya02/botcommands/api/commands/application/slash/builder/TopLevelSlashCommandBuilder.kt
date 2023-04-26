package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelSlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.TopLevelSlashCommandBuilderMixin
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.SimpleCommandMap
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.KFunction

class TopLevelSlashCommandBuilder internal constructor(
    context: BContextImpl,
    name: String,
    function: KFunction<Any>?,
    scope: CommandScope
) : SlashCommandBuilder(context, name, function), ITopLevelSlashCommandBuilder by TopLevelSlashCommandBuilderMixin(scope) {
    override val topLevelBuilder: ITopLevelApplicationCommandBuilder = this
    override val parentInstance: INamedCommand? = null

    @get:JvmSynthetic
    internal val subcommands: SimpleCommandMap<SlashSubcommandBuilder> = SimpleCommandMap.ofBuilders()
    @get:JvmSynthetic
    internal val subcommandGroups: SimpleCommandMap<SlashSubcommandGroupBuilder> = SimpleCommandMap(null)

    override val allowOptions: Boolean
        get() = subcommands.isEmpty() && subcommandGroups.isEmpty()
    override val allowSubcommands: Boolean
        get() = commandOptionBuilders.isEmpty()
    override val allowSubcommandGroups: Boolean
        get() = commandOptionBuilders.isEmpty()

    fun subcommand(name: String, function: KFunction<Any>, block: SlashSubcommandBuilder.() -> Unit) {
        if (isFunctionSet()) throwUser("Cannot add subcommands as this already contains a function")
        if (!allowSubcommands) throwUser("Cannot add subcommands as this already contains options")

        SlashSubcommandBuilder(context, name, function, this, this).apply(block).also(subcommands::putNewCommand)
    }

    fun subcommandGroup(name: String, block: SlashSubcommandGroupBuilder.() -> Unit) {
        if (isFunctionSet()) throwUser("Cannot add subcommand groups as this already contains a function")
        if (!allowSubcommandGroups) throwUser("Cannot add subcommand groups as this already contains options")

        SlashSubcommandGroupBuilder(context, name, this).apply(block).also(subcommandGroups::putNewCommand)
    }

    internal fun build(): TopLevelSlashCommandInfo {
        return TopLevelSlashCommandInfo(context, this)
    }

    private fun isFunctionSet() = function !== theFakeFunction
}
