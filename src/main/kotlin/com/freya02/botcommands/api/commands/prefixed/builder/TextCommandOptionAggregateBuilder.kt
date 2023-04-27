package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.asDiscordString
import kotlin.reflect.KFunction

class TextCommandOptionAggregateBuilder(owner: KFunction<*>, declaredName: String, aggregator: KFunction<*>) : OptionAggregateBuilder(owner, declaredName, aggregator) {
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        this += TextCommandOptionBuilder(owner, declaredName, optionName).apply(block)
    }

    fun customOption(declaredName: String) {
        this += CustomOptionBuilder(owner, declaredName)
    }

    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        this += TextGeneratedOptionBuilder(owner, declaredName, generatedValueSupplier)
    }
}
