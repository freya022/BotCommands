package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationOptionRegistry
import io.github.freya022.botcommands.api.core.options.builder.inlineClassAggregate
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KClass

interface SlashOptionRegistry : ApplicationOptionRegistry<SlashCommandOptionAggregateBuilder> {
    /**
     * Declares an input option, supported types and modifiers are in [ParameterResolver],
     * additional types can be added by implementing [SlashParameterResolver].
     *
     * @param declaredName Name of the declared parameter which receives the value
     * @param optionName   Name of the option on Discord,
     * transforms the declared name uppercase characters with underscore + lowercase by default
     */
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {})

    /**
     * Declares multiple input options in a single parameter.
     *
     * The parameter's type needs to be a [List],
     * where the element type is supported by [ParameterResolver].
     *
     * Additional types can be added by implementing [SlashParameterResolver].
     *
     * @param declaredName       Name of the declared parameter which receives the value of the combined options
     * @param amount             How many options to generate
     * @param requiredAmount     How many of the generated options are required
     * @param optionNameSupplier Block generating an option name from the option's index
     *
     * @see VarArgs
     */
    fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit = {})
}

/**
 * Declares an input option encapsulated in an inline class.
 *
 * Supported types can be found in [ParameterResolver],
 * additional types can be added by implementing [SlashParameterResolver].
 *
 * @param declaredName Name of the declared parameter which receives the value class
 * @param optionName   Name of the option on Discord,
 * transforms the declared name uppercase characters with underscore + lowercase by default
 * @param clazz        The inline class type
 */
fun SlashOptionRegistry.inlineClassOption(declaredName: String, optionName: String? = null, clazz: KClass<*>, block: SlashCommandOptionBuilder.() -> Unit = {}) {
    inlineClassAggregate(declaredName, clazz) { valueName ->
        option(valueName, optionName ?: valueName.toDiscordString(), block)
    }
}

/**
 * Declares an input option encapsulated in an inline class.
 *
 * Supported types and modifiers are in [ParameterResolver],
 * additional types can be added by implementing [SlashParameterResolver].
 *
 * @param declaredName Name of the declared parameter which receives the value class
 * @param optionName   Name of the option on Discord,
 * transforms the declared name uppercase characters with underscore + lowercase by default
 *
 * @param T            The inline class type
 */
inline fun <reified T : Any> SlashOptionRegistry.inlineClassOption(declaredName: String, optionName: String? = null, noinline block: SlashCommandOptionBuilder.() -> Unit = {}) {
    inlineClassOption(declaredName, optionName, T::class, block)
}

/**
 * Declares multiple input options encapsulated in an inline class.
 *
 * The property of the inline class needs to be a [List],
 * where the element type is supported by [ParameterResolver].
 *
 * Additional types can be added by implementing [SlashParameterResolver].
 *
 * @param declaredName       Name of the declared parameter which receives the value class
 * @param clazz              The inline class type
 * @param amount             How many options to generate
 * @param requiredAmount     How many of the generated options are required
 * @param optionNameSupplier Block generating an option name from the option's index
 *
 * @see VarArgs
 */
fun SlashOptionRegistry.inlineClassOptionVararg(declaredName: String, clazz: KClass<*>, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
    inlineClassAggregate(declaredName, clazz) { valueName ->
        optionVararg(valueName, amount, requiredAmount, optionNameSupplier, block)
    }
}

/**
 * Declares multiple input options encapsulated in an inline class.
 *
 * The property of the inline class needs to be a [List],
 * where the element type is supported by [ParameterResolver].
 *
 * Additional types can be added by implementing [SlashParameterResolver].
 *
 * @param declaredName       Name of the declared parameter which receives the value class
 * @param amount             How many options to generate
 * @param requiredAmount     How many of the generated options are required
 * @param optionNameSupplier Block generating an option name from the option's index
 *
 * @param T                  The inline class type
 *
 * @see VarArgs
 */
inline fun <reified T : Any> SlashOptionRegistry.inlineClassOptionVararg(declaredName: String, amount: Int, requiredAmount: Int, noinline optionNameSupplier: (Int) -> String, noinline block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
    inlineClassOptionVararg(declaredName, T::class, amount, requiredAmount, optionNameSupplier, block)
}