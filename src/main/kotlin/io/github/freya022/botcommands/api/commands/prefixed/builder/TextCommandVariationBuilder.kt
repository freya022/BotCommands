package io.github.freya022.botcommands.api.commands.prefixed.builder

import io.github.freya022.botcommands.api.commands.application.slash.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.freya022.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

@CommandDSL
class TextCommandVariationBuilder internal constructor(
    private val context: BContext,
    function: KFunction<Any>
) : IBuilderFunctionHolder<Any> {
    override val function: KFunction<Any> = function.reflectReference()

    private val _optionAggregateBuilders = OptionAggregateBuildersImpl(function) { aggregatorParameter, aggregator ->
        TextCommandOptionAggregateBuilder(aggregatorParameter, aggregator)
    }

    internal val optionAggregateBuilders: Map<String, TextCommandOptionAggregateBuilder>
        get() = _optionAggregateBuilders.optionAggregateBuilders

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    fun inlineClassOption(declaredName: String, optionName: String? = null, clazz: Class<*>, block: TextCommandOptionBuilder.() -> Unit) {
        val aggregatorConstructor = clazz.kotlin.primaryConstructor
            ?: throwUser("Found no public constructor for class ${clazz.simpleNestedName}")
        aggregate(declaredName, aggregatorConstructor) {
            val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
                ?: throwUser(aggregatorConstructor, "Constructor must only have one parameter")
            option(parameterName, optionName ?: parameterName.toDiscordString(), block)
        }
    }

    inline fun <reified T : Any> inlineClassOption(declaredName: String, optionName: String? = null, noinline block: TextCommandOptionBuilder.() -> Unit) {
        inlineClassOption(declaredName, optionName, T::class.java, block)
    }

    /**
     * @see VarArgs
     */
    fun inlineClassOptionVararg(declaredName: String, clazz: Class<*>, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: TextCommandOptionBuilder.(Int) -> Unit = {}) {
        val aggregatorConstructor = clazz.kotlin.primaryConstructor
            ?: throwUser("Found no public constructor for class ${clazz.simpleNestedName}")
        aggregate(declaredName, aggregatorConstructor) {
            val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
                ?: throwUser(aggregatorConstructor, "Constructor must only have one parameter")
            nestedOptionVararg(parameterName, amount, requiredAmount, optionNameSupplier, block)
        }
    }

    /**
     * @see VarArgs
     */
    inline fun <reified T : Any> inlineClassOptionVararg(declaredName: String, amount: Int, requiredAmount: Int, noinline optionNameSupplier: (Int) -> String, noinline block: TextCommandOptionBuilder.(Int) -> Unit = {}) {
        inlineClassOptionVararg(declaredName, T::class.java, amount, requiredAmount, optionNameSupplier, block)
    }

    /**
     * @see VarArgs
     */
    @JvmOverloads
    fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: TextCommandOptionBuilder.(Int) -> Unit = {}) {
        if (_optionAggregateBuilders.hasVararg())
            throwUser("Cannot have more than 1 vararg in text commands")

        //Same as in SlashCommandBuilder#optionVararg
        _optionAggregateBuilders.varargAggregate(declaredName) {
            for (i in 0..<amount) {
                option("args", optionNameSupplier(i)) {
                    block(i)
                    isOptional = i >= requiredAmount
                }
            }
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: TextCommandOptionAggregateBuilder.() -> Unit = {}) {
        _optionAggregateBuilders.aggregate(declaredName, aggregator, block)
    }

    private fun selfAggregate(declaredName: String, block: TextCommandOptionAggregateBuilder.() -> Unit) {
        _optionAggregateBuilders.selfAggregate(declaredName, block)
    }

    internal fun build(info: TextCommandInfo): TextCommandVariation {
        return TextCommandVariation(context, info, this)
    }
}
