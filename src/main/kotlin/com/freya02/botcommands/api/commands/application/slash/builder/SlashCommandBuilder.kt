package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandParameter
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.internal.utils.Checks

abstract class SlashCommandBuilder internal constructor(
    protected val context: BContextImpl,
    name: String
) : ApplicationCommandBuilder(name) {
    var description: String = DEFAULT_DESCRIPTION

    protected abstract val allowOptions: Boolean
    protected abstract val allowSubcommands: Boolean
    protected abstract val allowSubcommandGroups: Boolean

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun aggregate(declaredName: String, block: SlashCommandOptionAggregateBuilder.() -> Unit = {}) {
        if (!allowOptions) throwUser("Cannot add options as this already contains subcommands/subcommand groups")

        optionAggregateBuilders[declaredName] = SlashCommandOptionAggregateBuilder(context, declaredName)
            .apply(block)
            .checkFunction()
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        aggregate(declaredName) {
            option(declaredName, optionName, block)
            aggregator = ::singleAggregator
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun customOption(declaredName: String) {
        aggregate(declaredName) { //TODO Make custom aggregate and remove fake aggregator
            customOption(declaredName)
            aggregator = ::fakeAggregator //unused
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        aggregate(declaredName) { //TODO Make custom aggregate and remove fake aggregator
            generatedOption(declaredName, generatedValueSupplier)
            aggregator = ::fakeAggregator //unused
        }
    }

    companion object {
        const val DEFAULT_DESCRIPTION = "No description"

        suspend fun singleAggregator(
            context: BContext,
            info: SlashCommandInfo,
            event: CommandInteractionPayload,
            mappings: Map<String, OptionMapping>,
            commandParameter: SlashCommandParameter
        ) = commandParameter.resolver.resolveSuspend(context, info, event, mappings.values.first())

        @Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
        suspend fun fakeAggregator(
            context: BContext,
            info: SlashCommandInfo,
            event: CommandInteractionPayload,
            mappings: Map<String, OptionMapping>,
            commandParameter: SlashCommandParameter
        ): Any = throwInternal("This aggregator should not be used for MethodParameterType ${commandParameter.methodParameterType}")
    }
}
