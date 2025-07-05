package io.github.freya022.botcommands.internal.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver

internal interface SlashCommandOptionMixin : SlashCommandOption {
    val resolver: SlashParameterResolver<*, *>
}