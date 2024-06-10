package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.text.TextCommandVariationImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwUser
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
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    fun inlineClassOption(declaredName: String, optionName: String? = null, clazz: KClass<*>, block: TextCommandOptionBuilder.() -> Unit = {}) {
        val aggregatorConstructor = clazz.primaryConstructor
            ?: throwUser("Found no public constructor for class ${clazz.simpleNestedName}")
        aggregate(declaredName, aggregatorConstructor) {
            val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
                ?: throwUser(aggregatorConstructor, "Constructor must only have one parameter")
            option(parameterName, optionName ?: parameterName.toDiscordString(), block)
        }
    }

    inline fun <reified T : Any> inlineClassOption(declaredName: String, optionName: String? = null, noinline block: TextCommandOptionBuilder.() -> Unit = {}) {
        inlineClassOption(declaredName, optionName, T::class, block)
    }

    /**
     * @see VarArgs
     */
    fun inlineClassOptionVararg(declaredName: String, clazz: KClass<*>, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: TextCommandOptionBuilder.(Int) -> Unit = {}) {
        val aggregatorConstructor = clazz.primaryConstructor
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
        inlineClassOptionVararg(declaredName, T::class, amount, requiredAmount, optionNameSupplier, block)
    }

    /**
     * @see VarArgs
     */
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
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: TextCommandOptionAggregateBuilder.() -> Unit = {}) {
        _optionAggregateBuilders.aggregate(declaredName, aggregator, block)
    }

    private fun selfAggregate(declaredName: String, block: TextCommandOptionAggregateBuilder.() -> Unit) {
        _optionAggregateBuilders.selfAggregate(declaredName, block)
    }

    internal fun build(info: TextCommandInfoImpl): TextCommandVariationImpl {
        return TextCommandVariationImpl(context, info, this)
    }
}

inline fun <reified T : TextCommandFilter<*>> TextCommandVariationBuilder.filter(): T {
    return context.getService<T>()
}
