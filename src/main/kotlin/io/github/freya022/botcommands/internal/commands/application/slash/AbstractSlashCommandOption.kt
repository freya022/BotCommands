package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandOptionImpl

internal abstract class AbstractSlashCommandOption internal constructor(
    optionBuilder: SlashCommandOptionBuilder,
    internal val resolver: SlashParameterResolver<*, *>
) : ApplicationCommandOptionImpl(optionBuilder) {
    abstract override val command: SlashCommandInfo

    val discordName = optionBuilder.optionName
}