package io.github.freya022.botcommands.internal.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.setCallerAsDeclarationSite
import io.github.freya022.botcommands.internal.commands.application.NamedCommandMap
import io.github.freya022.botcommands.internal.commands.application.slash.SlashSubcommandGroupInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfoImpl
import io.github.freya022.botcommands.internal.utils.lazyPath
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KFunction

internal class SlashSubcommandGroupBuilderImpl internal constructor(
    private val context: BContext,
    override val name: String,
    private val topLevelBuilder: TopLevelSlashCommandBuilderImpl
) : SlashSubcommandGroupBuilder {
    override val parentInstance: INamedCommand = topLevelBuilder
    override val path: CommandPath by lazyPath()
    override lateinit var declarationSite: DeclarationSite

    internal val subcommands: NamedCommandMap<SlashSubcommandBuilderImpl> = NamedCommandMap()

    override var description: String? = null
        set(value) {
            require(value == null || value.isNotBlank()) { "Description cannot be blank" }
            field = value
        }

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

    override fun subcommand(name: String, function: KFunction<Any>, block: SlashSubcommandBuilder.() -> Unit) {
        SlashSubcommandBuilderImpl(context, name, function, topLevelBuilder, this)
            .setCallerAsDeclarationSite()
            .apply(block)
            .also(subcommands::putNewCommand)
    }

    internal fun build(topLevelInstance: TopLevelSlashCommandInfoImpl): SlashSubcommandGroupInfoImpl {
        return SlashSubcommandGroupInfoImpl(topLevelInstance, this)
    }
}