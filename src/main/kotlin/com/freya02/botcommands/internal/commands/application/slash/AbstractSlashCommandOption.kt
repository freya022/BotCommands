package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.ApplicationCommandOption

abstract class AbstractSlashCommandOption(
    optionBuilder: SlashCommandOptionBuilder,
    final override val resolver: SlashParameterResolver<*, *>
) : ApplicationCommandOption(optionBuilder) {
    val discordName = optionBuilder.optionName
}