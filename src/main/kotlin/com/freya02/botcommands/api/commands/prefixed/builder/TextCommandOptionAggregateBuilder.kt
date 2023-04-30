package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.parameters.MultiParameter
import kotlin.reflect.KFunction

class TextCommandOptionAggregateBuilder(multiParameter: MultiParameter, aggregator: KFunction<*>) : OptionAggregateBuilder(multiParameter, aggregator) {
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        this += TextCommandOptionBuilder(multiParameter, optionName).apply(block)
    }

    fun customOption(declaredName: String) {
        this += CustomOptionBuilder(multiParameter)
    }

    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        this += TextGeneratedOptionBuilder(multiParameter, generatedValueSupplier)
    }
}
