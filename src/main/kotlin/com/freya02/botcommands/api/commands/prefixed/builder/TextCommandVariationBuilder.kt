package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.builder.BuilderFunctionHolder
import com.freya02.botcommands.api.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation

class TextCommandVariationBuilder internal constructor(private val context: BContextImpl) : BuilderFunctionHolder<Any>() {
    @get:JvmSynthetic
    internal val optionBuilders: MutableMap<String, OptionBuilder> = mutableMapOf()

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: TextOptionBuilder.() -> Unit = {}) {
        optionBuilders[declaredName] = TextOptionBuilder(declaredName, optionName).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun customOption(declaredName: String) {
        optionBuilders[declaredName] = CustomOptionBuilder(declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        optionBuilders[declaredName] = TextGeneratedOptionBuilder(declaredName, generatedValueSupplier)
    }

    @JvmSynthetic
    internal fun build(info: TextCommandInfo): TextCommandVariation {
        checkFunction()

        return TextCommandVariation(context, info, this)
    }
}