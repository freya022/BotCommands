package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.options

import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.commands.application.slash.options.SlashCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.SlashCommandOptionMixin
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.utils.requireAt
import net.dv8tion.jda.api.interactions.commands.OptionType

private val unsupportedTypes = enumSetOf(
    OptionType.ATTACHMENT,
    OptionType.ROLE,
    OptionType.USER,
    OptionType.CHANNEL,
    OptionType.MENTIONABLE,
)

internal class AutocompleteCommandOptionImpl internal constructor(
    override val parent: AutocompleteCommandParameterImpl,
    private val slashOption: SlashCommandOptionImpl
) : OptionImpl(slashOption),
    SlashCommandOptionMixin {

    override val executable get() = parent.executable

    override val resolver get() = slashOption.resolver
    override val discordName get() = slashOption.discordName
    override val description get() = slashOption.description
    override val usePredefinedChoices get() = slashOption.usePredefinedChoices
    override val choices get() = slashOption.choices
    override val range get() = slashOption.range
    override val length get() = slashOption.length

    init {
        requireAt(resolver.optionType !in unsupportedTypes, executable.function) {
            "Autocomplete parameters does not support option type ${resolver.optionType} as Discord does not send them"
        }
    }

    override fun hasAutocomplete() = true

    override fun invalidateAutocomplete() = slashOption.invalidateAutocomplete()
}