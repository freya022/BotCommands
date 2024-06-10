package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.INamedCommand.Companion.computePath
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashCommandGroupData
import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.commands.application.NamedCommandMap
import io.github.freya022.botcommands.internal.commands.application.slash.SlashSubcommandGroupInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfoImpl
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KFunction

@CommandDSL
class SlashSubcommandGroupBuilder internal constructor(
    private val context: BContext,
    override val name: String,
    private val topLevelBuilder: TopLevelSlashCommandBuilder
) : INamedCommand, IDeclarationSiteHolderBuilder {
    override val parentInstance: INamedCommand = topLevelBuilder
    override val path: CommandPath by lazy { computePath() }
    override lateinit var declarationSite: DeclarationSite

    internal val subcommands: NamedCommandMap<SlashSubcommandBuilder> = NamedCommandMap()

    //TODO change docs when Discord eventually decides to not have a mess of a command list
    /**
     * Short description of the subcommand group.
     * May be displayed on Discord.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped,
     * for example, on `/ban temp recent` => `ban.temp.description`.
     *
     * @see LocalizationFunction
     *
     * @see SlashCommandGroupData.description
     */
    var description: String? = null
        set(value) {
            require(value == null || value.isNotBlank()) { "Description cannot be blank" }
            field = value
        }

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

    /**
     * Adds a subcommand, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see JDASlashCommand.subcommand
     */
    fun subcommand(name: String, function: KFunction<Any>, block: SlashSubcommandBuilder.() -> Unit = {}) {
        SlashSubcommandBuilder(context, name, function, topLevelBuilder, this)
            .setCallerAsDeclarationSite()
            .apply(block)
            .also(subcommands::putNewCommand)
    }

    internal fun build(topLevelInstance: TopLevelSlashCommandInfoImpl): SlashSubcommandGroupInfoImpl {
        return SlashSubcommandGroupInfoImpl(topLevelInstance, this)
    }
}
