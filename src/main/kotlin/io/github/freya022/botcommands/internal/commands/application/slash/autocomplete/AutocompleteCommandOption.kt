package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.AbstractSlashCommandOption
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.requireUser
import net.dv8tion.jda.api.interactions.commands.OptionType

private val unsupportedTypes = enumSetOf(
    OptionType.ATTACHMENT,
    OptionType.ROLE,
    OptionType.USER,
    OptionType.CHANNEL,
    OptionType.MENTIONABLE,
)

class AutocompleteCommandOption(
    optionBuilder: SlashCommandOptionBuilder,
    resolver: SlashParameterResolver<*, *>
) : AbstractSlashCommandOption(optionBuilder, resolver) {
    init {
        requireUser(resolver.optionType !in unsupportedTypes, optionBuilder.parameter.function) {
            "Autocomplete parameters does not support option type ${resolver.optionType} as Discord does not send them"
        }
    }
}