package io.github.freya022.botcommands.internal.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.options.ApplicationCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionBuilderImpl

internal abstract class AbstractSlashCommandOption internal constructor(
    optionBuilder: SlashCommandOptionBuilderImpl,
    override val resolver: SlashParameterResolver<*, *>
) : ApplicationCommandOptionImpl(optionBuilder),
    SlashCommandOptionMixin {

    abstract override val executable: SlashCommandInfo

    override val discordName = optionBuilder.optionName
}