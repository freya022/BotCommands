package io.github.freya022.botcommands.internal.commands.text.builder

import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.commands.text.options.builder.TextCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.text.options.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.text.TextCommandVariationImpl
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixin
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixinImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KFunction

internal class TextCommandVariationBuilderImpl internal constructor(
    override val context: BContext,
    function: KFunction<Any>
) : TextCommandVariationBuilder,
    IBuilderFunctionHolder<Any>,
    OptionAggregateBuilderContainerMixin<TextCommandOptionAggregateBuilder> {

    override lateinit var declarationSite: DeclarationSite
    override val function: KFunction<Any> = function.reflectReference()

    private val aggregateContainer =
        OptionAggregateBuilderContainerMixinImpl(function) { aggregatorParameter, aggregator ->
            TextCommandOptionAggregateBuilderImpl(context, this, aggregatorParameter, aggregator)
        }

    override val optionAggregateBuilders: Map<String, TextCommandOptionAggregateBuilder>
        get() = aggregateContainer.optionAggregateBuilders

    override val filters: MutableList<TextCommandFilter<*>> = arrayListOf()

    override var description: String? = null

    override var usage: String? = null

    override var example: String? = null

    override fun hasVararg(): Boolean = aggregateContainer.hasVararg()

    override fun option(declaredName: String, optionName: String, block: TextCommandOptionBuilder.() -> Unit) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    override fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: TextCommandOptionBuilder.(Int) -> Unit) {
        if (aggregateContainer.hasVararg())
            throwArgument("Cannot have more than 1 vararg in text commands")

        //Same as in SlashCommandBuilder#optionVararg
        aggregateContainer.varargAggregate(declaredName) {
            for (i in 0..<amount) {
                option("args", optionNameSupplier(i)) {
                    block(i)
                    (this as TextCommandOptionBuilderImpl).isOptional = i >= requiredAmount
                }
            }
        }
    }

    override fun serviceOption(declaredName: String) {
        selfAggregate(declaredName) {
            serviceOption(declaredName)
        }
    }

    override fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }

    override fun aggregate(declaredName: String, aggregator: KFunction<*>, block: TextCommandOptionAggregateBuilder.() -> Unit) =
        aggregateContainer.aggregate(declaredName, aggregator, block)

    override fun selfAggregate(declaredName: String, block: TextCommandOptionAggregateBuilder.() -> Unit) =
        aggregateContainer.selfAggregate(declaredName, block)

    override fun varargAggregate(declaredName: String, block: TextCommandOptionAggregateBuilder.() -> Unit) =
        aggregateContainer.varargAggregate(declaredName, block)

    internal fun build(info: TextCommandInfoImpl): TextCommandVariationImpl {
        return TextCommandVariationImpl(context, info, this)
    }
}