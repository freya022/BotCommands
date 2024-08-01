package io.github.freya022.botcommands.internal.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.TopLevelSlashCommandBuilderMixin
import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.NamedCommandMap
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.isFakeSlashFunction
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfoImpl
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KFunction

internal class TopLevelSlashCommandBuilderImpl internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>?,
    scope: CommandScope
) : SlashCommandBuilderImpl(context, name, function),
    TopLevelSlashCommandBuilder,
    //TODO uhhh
    ITopLevelSlashCommandBuilder by TopLevelSlashCommandBuilderMixin(scope) {

    override val topLevelBuilder: ITopLevelApplicationCommandBuilder get() = this
    override val parentInstance: INamedCommand? get() = null

    internal val subcommands: NamedCommandMap<SlashSubcommandBuilderImpl> = NamedCommandMap()
    internal val subcommandGroups: NamedCommandMap<SlashSubcommandGroupBuilderImpl> = NamedCommandMap()

    override val allowOptions: Boolean
        get() = subcommands.isEmpty() && subcommandGroups.isEmpty()
    override val allowSubcommands: Boolean
        get() = optionAggregateBuilders.isEmpty()
    override val allowSubcommandGroups: Boolean
        get() = optionAggregateBuilders.isEmpty()

    override fun subcommand(name: String, function: KFunction<Any>, block: SlashSubcommandBuilder.() -> Unit) {
        if (isFunctionSet()) throwArgument("Cannot add subcommands as this already contains a function")
        if (!allowSubcommands) throwArgument("Cannot add subcommands as this already contains options")

        SlashSubcommandBuilderImpl(context, name, function, this, this)
            .setCallerAsDeclarationSite()
            .apply(block)
            .also(subcommands::putNewCommand)
    }

    override fun subcommandGroup(name: String, block: SlashSubcommandGroupBuilder.() -> Unit) {
        if (isFunctionSet()) throwArgument("Cannot add subcommand groups as this already contains a function")
        if (!allowSubcommandGroups) throwArgument("Cannot add subcommand groups as this already contains options")

        SlashSubcommandGroupBuilderImpl(context, name, this)
            .setCallerAsDeclarationSite()
            .apply(block)
            .also(subcommandGroups::putNewCommand)
    }

    internal fun build(): TopLevelSlashCommandInfoImpl {
        return TopLevelSlashCommandInfoImpl(context, this)
    }

    private fun isFunctionSet() = !function.isFakeSlashFunction()
}