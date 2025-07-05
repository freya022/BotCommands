package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.options

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandParameter
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.commands.application.slash.options.SlashCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.SlashCommandParameterImpl
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.isNullable
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KFunction
import kotlin.reflect.full.findParameterByName

private val unsupportedTypes = enumSetOf(
    OptionType.ATTACHMENT,
    OptionType.ROLE,
    OptionType.USER,
    OptionType.CHANNEL,
    OptionType.MENTIONABLE,
)

internal class AutocompleteCommandParameterImpl internal constructor(
    private val slashParameter: SlashCommandParameterImpl,
    autocompleteFunction: KFunction<*>
) : AbstractMethodParameter(slashParameter.kParameter),
    AggregatedParameterMixin,
    SlashCommandParameter {

    override val executable get() = slashParameter.executable
    override val aggregator get() = slashParameter.aggregator

    override val executableParameter =
        autocompleteFunction.findParameterByName(name)
            ?: throwInternal(
                "Parameter from autocomplete function '${kParameter.name}' should have been found on slash command ${slashParameter.executable.function.shortSignature}"
            )

    init {
        require(executableParameter.isNullable == kParameter.isNullable) {
            """
                Parameter '${kParameter.name}' from autocomplete function should have same nullability as on slash command
                Autocomplete function: ${autocompleteFunction.shortSignature}
                Slash command function: ${slashParameter.executable.function.shortSignature}
            """.trimIndent()
        }
    }

    override val nestedAggregatedParameters = slashParameter.nestedAggregatedParameters.map {
        // Would be better if we could just ask the original parameter to compute the value
        // but this works too I guess
        AutocompleteCommandParameterImpl(it, it.executableParameter.function)
    }

    override val options = slashParameter.options.onEach {
        if (it is SlashCommandOptionImpl) {
            requireAt(it.resolver.optionType !in unsupportedTypes, executable.function) {
                "Autocomplete parameters does not support option type ${it.resolver.optionType} as Discord does not send them"
            }
        }
    }
}