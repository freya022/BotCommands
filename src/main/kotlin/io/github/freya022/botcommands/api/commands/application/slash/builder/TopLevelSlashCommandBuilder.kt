package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.TopLevelSlashCommandBuilderMixin
import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.NamedCommandMap
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.isFakeSlashFunction
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfoImpl
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import kotlin.reflect.KFunction

class TopLevelSlashCommandBuilder internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>?,
    scope: CommandScope
) : SlashCommandBuilder(context, name, function), ITopLevelSlashCommandBuilder by TopLevelSlashCommandBuilderMixin(scope) {
    override val topLevelBuilder: ITopLevelApplicationCommandBuilder get() = this
    override val parentInstance: INamedCommand? get() = null

    internal val subcommands: NamedCommandMap<SlashSubcommandBuilder> = NamedCommandMap()
    internal val subcommandGroups: NamedCommandMap<SlashSubcommandGroupBuilder> = NamedCommandMap()

    override val allowOptions: Boolean
        get() = subcommands.isEmpty() && subcommandGroups.isEmpty()
    override val allowSubcommands: Boolean
        get() = optionAggregateBuilders.isEmpty()
    override val allowSubcommandGroups: Boolean
        get() = optionAggregateBuilders.isEmpty()

    /**
     * Adds a subcommand, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see JDASlashCommand.subcommand
     */
    fun subcommand(name: String, function: KFunction<Any>, block: SlashSubcommandBuilder.() -> Unit = {}) {
        if (isFunctionSet()) throwUser("Cannot add subcommands as this already contains a function")
        if (!allowSubcommands) throwUser("Cannot add subcommands as this already contains options")

        SlashSubcommandBuilder(context, name, function, this, this)
            .setCallerAsDeclarationSite()
            .apply(block)
            .also(subcommands::putNewCommand)
    }

    /**
     * Adds a subcommand group, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     *
     * @see JDASlashCommand.group
     */
    fun subcommandGroup(name: String, block: SlashSubcommandGroupBuilder.() -> Unit) {
        if (isFunctionSet()) throwUser("Cannot add subcommand groups as this already contains a function")
        if (!allowSubcommandGroups) throwUser("Cannot add subcommand groups as this already contains options")

        SlashSubcommandGroupBuilder(context, name, this)
            .setCallerAsDeclarationSite()
            .apply(block)
            .also(subcommandGroups::putNewCommand)
    }

    internal fun build(): TopLevelSlashCommandInfoImpl {
        return TopLevelSlashCommandInfoImpl(context, this)
    }

    private fun isFunctionSet() = !function.isFakeSlashFunction()
}
