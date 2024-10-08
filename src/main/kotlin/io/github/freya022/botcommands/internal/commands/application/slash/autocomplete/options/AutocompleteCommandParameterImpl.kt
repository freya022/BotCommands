package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.options

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandParameter
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.commands.application.slash.options.SlashCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.SlashCommandParameterImpl
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isNullable
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
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
    private val parameter: SlashCommandParameterImpl,
    autocompleteFunction: KFunction<*>
) : AbstractMethodParameter(parameter.kParameter),
    AggregatedParameterMixin,
    SlashCommandParameter {

    override val executable get() = parameter.executable
    override val aggregator get() = parameter.aggregator

    override val executableParameter =
        autocompleteFunction.findParameterByName(name)
            ?: throwInternal(
                "Parameter from autocomplete function '${kParameter.name}' should have been found on slash command ${parameter.executable.function.shortSignature}"
            )

    init {
        requireAt(executableParameter.isNullable == kParameter.isNullable, autocompleteFunction) {
            """
                Parameter '${kParameter.name}' from autocomplete function should have same nullability as on slash command
                Autocomplete function: ${autocompleteFunction.shortSignature}
                Slash command function: ${parameter.executable.function.shortSignature}
            """.trimIndent()
        }
    }

    override val nestedAggregatedParameters = parameter.nestedAggregatedParameters.map {
        // Would be better if we could just ask the original parameter to compute the value
        // but this works too I guess
        AutocompleteCommandParameterImpl(it, it.executableParameter.function)
    }

    override val options = parameter.options.onEach {
        if (it is SlashCommandOptionImpl) {
            requireAt(it.resolver.optionType !in unsupportedTypes, executable.function) {
                "Autocomplete parameters does not support option type ${it.resolver.optionType} as Discord does not send them"
            }
        }
    }
}