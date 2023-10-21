package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandOption

abstract class AbstractSlashCommandOption(
    optionBuilder: SlashCommandOptionBuilder,
    final override val resolver: SlashParameterResolver<*, *>
) : ApplicationCommandOption(optionBuilder) {
    val discordName = optionBuilder.optionName
}