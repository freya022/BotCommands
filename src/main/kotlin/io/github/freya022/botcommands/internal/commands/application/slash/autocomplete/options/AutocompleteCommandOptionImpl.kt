package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.options

import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.options.AbstractSlashCommandOption
import io.github.freya022.botcommands.internal.commands.application.slash.options.AbstractSlashCommandParameter
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionBuilderImpl
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
    override val parent: AbstractSlashCommandParameter,
    optionBuilder: SlashCommandOptionBuilderImpl,
    resolver: SlashParameterResolver<*, *>
) : AbstractSlashCommandOption(optionBuilder, resolver) {

    override val executable get() = parent.executable

    init {
        requireAt(resolver.optionType !in unsupportedTypes, optionBuilder.parameter.function) {
            "Autocomplete parameters does not support option type ${resolver.optionType} as Discord does not send them"
        }
    }
}