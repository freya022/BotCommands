package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandParameter
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping

class SlashCommandOptionAggregateBuilder(
    private val context: BContextImpl,
    declaredName: String
) : ApplicationCommandOptionAggregateBuilder(declaredName) {
    //TODO this is incorrect, aggregates can contain other things than option mappings
    //  Could accept a KFunction here, with no distinction in parameters,
    //  the framework would just try to push the data it had declared in the DSL
    //  using MethodHandle#invokeWithArguments
    // This means that MethodParameters could analyse the types in the aggregator instead of the command function, using the aggregator's option builders
    @Deprecated("incorrect")
    lateinit var aggregator: suspend (
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        mappings: Map<String, OptionMapping>,
        commandParameter: SlashCommandParameter
    ) -> Any?

    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        optionBuilders[declaredName] = SlashCommandOptionBuilder(context, declaredName, optionName).apply(block)
    }

    override fun customOption(declaredName: String) {
        optionBuilders[declaredName] = CustomOptionBuilder(declaredName)
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        optionBuilders[declaredName] = ApplicationGeneratedOptionBuilder(declaredName, generatedValueSupplier)
    }

    @JvmSynthetic
    internal fun checkFunction() = this.apply {
        if (!::aggregator.isInitialized) {
            throwUser("An aggregated option must have its function set")
        }
    }
}