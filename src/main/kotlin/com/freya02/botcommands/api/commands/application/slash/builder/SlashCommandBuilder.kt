package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.annotations.IncludeClasspath
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.fakeSlashFunction
import com.freya02.botcommands.internal.parameters.MultiParameter
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KFunction

abstract class SlashCommandBuilder internal constructor(
    protected val context: BContextImpl,
    name: String,
    function: KFunction<Any>? //Nullable as subcommands make top level commands impossible to execute
) : ApplicationCommandBuilder<SlashCommandOptionAggregateBuilder>(name, function ?: fakeSlashFunction) {
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
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    @JvmOverloads
    fun optionVararg(declaredName: String, amount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
        aggregate(declaredName, Companion::varArgAggregator) {
            generatedOption("amount") { amount }

            for (i in 0..<amount) {
                option("args", optionNameSupplier(i)) {
                    block(i)
                }
            }
        }
    }

    override fun constructAggregate(multiParameter: MultiParameter, aggregator: KFunction<*>): SlashCommandOptionAggregateBuilder {
        if (!allowOptions) throwUser("Cannot add options as this already contains subcommands/subcommand groups")

        return SlashCommandOptionAggregateBuilder(context, multiParameter, aggregator)
    }

    @IncludeClasspath
    companion object {
        const val DEFAULT_DESCRIPTION = "No description"

        @JvmSynthetic
        @Suppress("UNUSED_PARAMETER")
        internal fun varArgAggregator(event: GuildSlashEvent, amount: Int, vararg args: Int): List<Int?> = args.toList().let {
            when {
                it.size < amount -> it + arrayOfNulls(amount - it.size)
                else -> it
            }
        }
    }
}
