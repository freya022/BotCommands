package io.github.freya022.botcommands.internal.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KFunction

internal abstract class SlashCommandBuilderImpl internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>? //Nullable as subcommands make top level commands impossible to execute
) : ApplicationCommandBuilderImpl<SlashCommandOptionAggregateBuilder>(context, name, function ?: SlashUtils.fakeSlashFunction),
    SlashCommandBuilder {

    final override val type: CommandType get() = CommandType.SLASH

    final override var description: String? = null
        set(value) {
            require(value == null || value.isNotBlank()) { "Description cannot be blank" }
            field = value
        }

    protected abstract val allowOptions: Boolean
    protected abstract val allowSubcommands: Boolean
    protected abstract val allowSubcommandGroups: Boolean

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

    final override fun option(declaredName: String, optionName: String, block: SlashCommandOptionBuilder.() -> Unit) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    final override fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit) {
        //Same as in TextCommandVariationBuilder#optionVararg
        varargAggregate(declaredName) {
            for (i in 0..<amount) {
                option("args", optionNameSupplier(i)) {
                    block(i)
                    (this as SlashCommandOptionBuilderImpl).isOptional = i >= requiredAmount
                }
            }
        }
    }

    final override fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): SlashCommandOptionAggregateBuilderImpl {
        if (!allowOptions) throwArgument("Cannot add options as this already contains subcommands/subcommand groups")

        return SlashCommandOptionAggregateBuilderImpl(context, this, aggregatorParameter, aggregator)
    }
}