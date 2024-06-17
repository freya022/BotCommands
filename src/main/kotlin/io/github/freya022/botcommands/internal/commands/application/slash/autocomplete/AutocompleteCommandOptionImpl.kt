package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.AbstractSlashCommandOption
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.requireAt
import net.dv8tion.jda.api.interactions.commands.OptionType

private val unsupportedTypes = enumSetOf(
    OptionType.ATTACHMENT,
    OptionType.ROLE,
    OptionType.USER,
    OptionType.CHANNEL,
    OptionType.MENTIONABLE,
)

internal class AutocompleteCommandOptionImpl(
    override val context: BContext,
    override val command: SlashCommandInfoImpl,
    optionBuilder: SlashCommandOptionBuilder,
    resolver: SlashParameterResolver<*, *>
) : AbstractSlashCommandOption(optionBuilder, resolver) {
    init {
        requireAt(resolver.optionType !in unsupportedTypes, optionBuilder.parameter.function) {
            "Autocomplete parameters does not support option type ${resolver.optionType} as Discord does not send them"
        }
    }
}