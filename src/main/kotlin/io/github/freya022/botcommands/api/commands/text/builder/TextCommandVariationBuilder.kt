package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.text.TextCommandVariationImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

@CommandDSL
class TextCommandVariationBuilder internal constructor(
    val context: BContext,
    function: KFunction<Any>
) : IBuilderFunctionHolder<Any>, IDeclarationSiteHolderBuilder {
    override lateinit var declarationSite: DeclarationSite
    override val function: KFunction<Any> = function.reflectReference()

    private val _optionAggregateBuilders = OptionAggregateBuildersImpl(function) { aggregatorParameter, aggregator ->
        TextCommandOptionAggregateBuilder(context, this, aggregatorParameter, aggregator)
    }

    internal val optionAggregateBuilders: Map<String, TextCommandOptionAggregateBuilder>
        get() = _optionAggregateBuilders.optionAggregateBuilders

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

    /**
     * Declares an input option, supported types and modifiers are in [ParameterResolver],
     * additional types can be added by implementing [TextParameterResolver].
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     * @param optionName   Name of the option on Discord,
     * transforms the declared name uppercase characters with underscore + lowercase by default
     */
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    /**
     * Declares an input option encapsulated in an inline class.
     *
     * Supported types can be found in [ParameterResolver],
     * additional types can be added by implementing [TextParameterResolver].
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     * @param optionName   Name of the option on Discord,
     * transforms the declared name uppercase characters with underscore + lowercase by default
     * @param clazz        The inline class type
     */
    fun inlineClassOption(declaredName: String, optionName: String? = null, clazz: KClass<*>, block: TextCommandOptionBuilder.() -> Unit = {}) {
        inlineClassAggregate(declaredName, clazz) { valueName ->
            option(valueName, optionName ?: valueName.toDiscordString(), block)
        }
    }

    /**
     * Declares an input option encapsulated in an inline class.
     *
     * Supported types can be found in [ParameterResolver],
     * additional types can be added by implementing [TextParameterResolver].
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     * @param optionName   Name of the option on Discord,
     * transforms the declared name uppercase characters with underscore + lowercase by default
     *
     * @param T            The inline class type
     */
    inline fun <reified T : Any> inlineClassOption(declaredName: String, optionName: String? = null, noinline block: TextCommandOptionBuilder.() -> Unit = {}) {
        inlineClassOption(declaredName, optionName, T::class, block)
    }

    /**
     * Declares multiple input options encapsulated in an inline class.
     *
     * The property of the inline class needs to be a [List],
     * where the element type is supported by [ParameterResolver].
     *
     * Additional types can be added by implementing [TextParameterResolver].
     *
     * @param declaredName       Name of the declared parameter in the [command function][function]
     * @param clazz              The inline class type
     * @param amount             How many options to generate
     * @param requiredAmount     How many of the generated options are required
     * @param optionNameSupplier Block generating an option name from the option's index
     *
     * @see VarArgs
     */
    fun inlineClassOptionVararg(declaredName: String, clazz: KClass<*>, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: TextCommandOptionBuilder.(Int) -> Unit = {}) {
        inlineClassAggregate(declaredName, clazz) { valueName ->
            nestedOptionVararg(valueName, amount, requiredAmount, optionNameSupplier, block)
        }
    }

    /**
     * Declares multiple input options encapsulated in an inline class.
     *
     * The property of the inline class needs to be a [List],
     * where the element type is supported by [ParameterResolver].
     *
     * Additional types can be added by implementing [TextParameterResolver].
     *
     * @param declaredName       Name of the declared parameter in the [command function][function]
     * @param amount             How many options to generate
     * @param requiredAmount     How many of the generated options are required
     * @param optionNameSupplier Block generating an option name from the option's index
     *
     * @param T                  The inline class type
     *
     * @see VarArgs
     */
    inline fun <reified T : Any> inlineClassOptionVararg(declaredName: String, amount: Int, requiredAmount: Int, noinline optionNameSupplier: (Int) -> String, noinline block: TextCommandOptionBuilder.(Int) -> Unit = {}) {
        inlineClassOptionVararg(declaredName, T::class, amount, requiredAmount, optionNameSupplier, block)
    }

    /**
     * Declares multiple input options in a single parameter.
     *
     * The parameter's type needs to be a [List],
     * where the element type is supported by [ParameterResolver].
     *
     * Additional types can be added by implementing [TextParameterResolver].
     *
     * @param declaredName       Name of the declared parameter in the [command function][function]
     * @param amount             How many options to generate
     * @param requiredAmount     How many of the generated options are required
     * @param optionNameSupplier Block generating an option name from the option's index
     *
     * @see VarArgs
     */
    fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: TextCommandOptionBuilder.(Int) -> Unit = {}) {
        if (_optionAggregateBuilders.hasVararg())
            throwArgument("Cannot have more than 1 vararg in text commands")

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
     * Declares a service option, allowing injection of services, which must be available.
     *
     * If the service is not available, then either don't declare this command,
     * or make the declaring class disabled by using one of:
     * - [@Condition][Condition]
     * - [@ConditionalService][ConditionalService]
     * - [@Dependencies][Dependencies]
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     */
    fun serviceOption(declaredName: String) {
        selfAggregate(declaredName) {
            serviceOption(declaredName)
        }
    }

    /**
     * Declares a custom option, such as an [TextLocalizationContext] (with [@LocalizationBundle][LocalizationBundle]).
     *
     * Additional types can be added by implementing [ICustomResolver].
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     */
    fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    /**
     * Declares a generated option, the supplier gets called on each command execution.
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     *
     * @see GeneratedOption @GeneratedOption
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }

    /**
     * Declares multiple options aggregated in a single parameter.
     *
     * The aggregator will receive all the options in the declared order and produce a single output.
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     * @param aggregator   The function taking all the options and merging them in a single output
     *
     * @see Aggregate @Aggregate
     */
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: TextCommandOptionAggregateBuilder.() -> Unit = {}) {
        _optionAggregateBuilders.aggregate(declaredName, aggregator, block)
    }

    fun inlineClassAggregate(declaredName: String, clazz: KClass<*>, block: TextCommandOptionAggregateBuilder.(valueName: String) -> Unit = {}) {
        val aggregatorConstructor = clazz.primaryConstructor
            ?: throwArgument("Found no public constructor for class ${clazz.simpleNestedName}")
        aggregate(declaredName, aggregatorConstructor) {
            val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
                ?: throwArgument(aggregatorConstructor, "Constructor must only have one parameter")
            block(parameterName)
        }
    }

    private fun selfAggregate(declaredName: String, block: TextCommandOptionAggregateBuilder.() -> Unit) {
        _optionAggregateBuilders.selfAggregate(declaredName, block)
    }

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
