package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder.Companion.DEFAULT_DESCRIPTION
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.commands.application.SimpleCommandMap
import io.github.freya022.botcommands.internal.commands.application.slash.SlashSubcommandGroupInfo
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KFunction

@CommandDSL
class SlashSubcommandGroupBuilder internal constructor(private val context: BContext, override val name: String, private val topLevelBuilder: TopLevelSlashCommandBuilder) : INamedCommand {
    override val parentInstance: INamedCommand = topLevelBuilder
    override val path: CommandPath by lazy { computePath() }

    internal val subcommands: SimpleCommandMap<SlashSubcommandBuilder> = SimpleCommandMap.ofBuilders()

    var description: String = DEFAULT_DESCRIPTION

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
        SlashSubcommandBuilder(context, name, function, topLevelBuilder, this).apply(block).also(subcommands::putNewCommand)
    }

    fun build(topLevelInstance: TopLevelSlashCommandInfo): SlashSubcommandGroupInfo {
        return SlashSubcommandGroupInfo(topLevelInstance, this)
    }
}
