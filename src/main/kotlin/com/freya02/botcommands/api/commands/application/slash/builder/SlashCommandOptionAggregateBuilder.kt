package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import kotlin.reflect.KFunction

class SlashCommandOptionAggregateBuilder(
    private val context: BContextImpl,
    owner: KFunction<*>,
    declaredName: String,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder(owner, declaredName, aggregator) {
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        this += SlashCommandOptionBuilder(context, owner, declaredName, optionName).apply(block)
    }

    override fun customOption(declaredName: String) {
        this += CustomOptionBuilder(owner, declaredName)
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(owner, declaredName, generatedValueSupplier)
    }
}