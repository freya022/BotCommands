package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.text.TextCommandVariationImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixin
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixinImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KFunction

@CommandDSL
class TextCommandVariationBuilder internal constructor(
    val context: BContext,
    function: KFunction<Any>
) : IBuilderFunctionHolder<Any>,
    IDeclarationSiteHolderBuilder,
    OptionAggregateBuilderContainerMixin<TextCommandOptionAggregateBuilder>,
    TextOptionRegistry {

    override lateinit var declarationSite: DeclarationSite
    override val function: KFunction<Any> = function.reflectReference()

    private val aggregateContainer = OptionAggregateBuilderContainerMixinImpl(function) { aggregatorParameter, aggregator ->
        TextCommandOptionAggregateBuilder(context, this, aggregatorParameter, aggregator)
    }

    override val optionAggregateBuilders: Map<String, TextCommandOptionAggregateBuilder>
        get() = aggregateContainer.optionAggregateBuilders

    /**
     * Set of filters preventing this command from executing.
     *
     * @see TextCommandFilter
     * @see TextCommandRejectionHandler
     */
    val filters: MutableList<TextCommandFilter<*>> = arrayListOf()

    /**
     * Short description of the command displayed in the built-in help command,
     * below the command usage.
     *
     * @see JDATextCommandVariation.description
     */
    var description: String? = null

    /**
     * Usage string for this command variation,
     * the built-in help command already sets the prefix and command name, with a space at the end.
     *
     * If not set, the built-in help command will generate a string out of the options.
     *
     * @see JDATextCommandVariation.usage
     */
    var usage: String? = null

    /**
     * Example command for this command variation,
     * the built-in help command already sets the prefix and command name, with a space at the end.
     *
     * If not set, the built-in help command will generate a string out of the options.
     *
     * @see JDATextCommandVariation.example
     */
    var example: String? = null

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
                    isOptional = i >= requiredAmount
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

/**
 * Convenience extension to load an [TextCommandFilter] service.
 *
 * Typically used as `filters += filter<MyApplicationCommandFilter>()`
 */
inline fun <reified T : TextCommandFilter<*>> TextCommandVariationBuilder.filter(): T {
    return context.getService<T>()
}
