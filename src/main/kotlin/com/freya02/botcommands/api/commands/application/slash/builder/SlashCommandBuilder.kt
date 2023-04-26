package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KFunction

abstract class SlashCommandBuilder internal constructor(
    protected val context: BContextImpl,
    name: String,
    function: KFunction<Any>? //Nullable as subcommands make top level commands impossible to execute
) : ApplicationCommandBuilder(name) {
    final override val function: KFunction<Any> = function ?: theFakeFunction

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
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: SlashCommandOptionAggregateBuilder.() -> Unit = {}) {
        aggregate(declaredName, aggregator, aggregator, block)
    }

    @JvmSynthetic
    internal fun selfAggregate(declaredName: String, block: SlashCommandOptionAggregateBuilder.() -> Unit) {
        //When the option needs to be searched on the command function instead of the aggregator
        aggregate(declaredName, function, ::singleAggregator, block)
    }

    private fun aggregate(declaredName: String, owner: KFunction<*>, aggregator: KFunction<*>, block: SlashCommandOptionAggregateBuilder.() -> Unit) {
        if (!allowOptions) throwUser("Cannot add options as this already contains subcommands/subcommand groups")

        optionAggregateBuilders[declaredName] =
            SlashCommandOptionAggregateBuilder(context, owner, declaredName, aggregator).apply(block)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }

    companion object {
        const val DEFAULT_DESCRIPTION = "No description"
        val theFakeFunction = ::fakeFunction

        private fun fakeFunction(event: GlobalSlashEvent): Nothing = throwInternal("Fake function was used")

        //The types should not matter as the checks are made against the command function
        fun singleAggregator(it: Any) = it
    }
}
