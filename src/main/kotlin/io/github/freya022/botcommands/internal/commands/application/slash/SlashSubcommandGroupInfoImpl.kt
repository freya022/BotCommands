package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.slash.SlashSubcommandGroupInfo
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashSubcommandGroupBuilderImpl
import io.github.freya022.botcommands.internal.utils.LocalizationUtils
import io.github.freya022.botcommands.internal.utils.lazyPath

internal class SlashSubcommandGroupInfoImpl(
    topLevelInstance: TopLevelSlashCommandInfoImpl,
    builder: SlashSubcommandGroupBuilderImpl
) : SlashSubcommandGroupInfo {
    override val parentInstance = topLevelInstance
    override val name = builder.name
    override val path: CommandPath by lazyPath()
    override val declarationSite: DeclarationSite = builder.declarationSite

    override val description: String = LocalizationUtils.getCommandDescription(topLevelInstance.context, builder, builder.description)

    override val subcommands: Map<String, SlashSubcommandInfoImpl> = builder.subcommands.map
        .mapValues { it.value.build(topLevelInstance, this) }
        .unmodifiableView()
}
