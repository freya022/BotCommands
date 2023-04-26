package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.api.commands.builder.BuilderFunctionHolder
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation

class TextCommandVariationBuilder internal constructor(private val context: BContextImpl) : BuilderFunctionHolder<Any>() {
    @get:JvmSynthetic
    internal val commandOptionBuilders: MutableMap<String, CommandOptionBuilder> = mutableMapOf()

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        //TODO use aggregates, don't forget to set the owner function as to respect the contract set in
        commandOptionBuilders[declaredName] = TextCommandOptionBuilder(function, declaredName, optionName).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun customOption(declaredName: String) {
        commandOptionBuilders[declaredName] = CustomOptionBuilder(function, declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        commandOptionBuilders[declaredName] = TextGeneratedOptionBuilder(function, declaredName, generatedValueSupplier)
    }

    @JvmSynthetic
    internal fun build(info: TextCommandInfo): TextCommandVariation {
        return TextCommandVariation(context, info, this)
    }
}
