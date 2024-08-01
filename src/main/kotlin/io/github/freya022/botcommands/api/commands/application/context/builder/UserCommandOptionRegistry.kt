package io.github.freya022.botcommands.api.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationOptionRegistry
import io.github.freya022.botcommands.api.core.options.builder.inlineClassAggregate
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import kotlin.reflect.KClass

interface UserCommandOptionRegistry : ApplicationOptionRegistry<UserCommandOptionAggregateBuilder> {
    /**
     * Declares an input option, supported types and modifiers are in [ParameterResolver],
     * additional types can be added by implementing [UserContextParameterResolver].
     *
     * @param declaredName Name of the declared parameter which receives the value of the combined options
     */
    fun option(declaredName: String)
}

/**
 * Declares an input option encapsulated in an inline class.
 *
 * Supported types can be found in [ParameterResolver],
 * additional types can be added by implementing [UserContextParameterResolver].
 *
 * @param declaredName Name of the declared parameter which receives the value class
 * @param clazz        The inline class type
 */
fun UserCommandOptionRegistry.inlineClassOption(declaredName: String, clazz: KClass<*>) {
    inlineClassAggregate(declaredName, clazz) { valueName ->
        option(valueName)
    }
}

/**
 * Declares an input option encapsulated in an inline class.
 *
 * Supported types and modifiers are in [ParameterResolver],
 * additional types can be added by implementing [UserContextParameterResolver].
 *
 * @param declaredName Name of the declared parameter which receives the value class
 *
 * @param T            The inline class type
 */
inline fun <reified T : Any> UserCommandOptionRegistry.inlineClassOption(declaredName: String) {
    inlineClassOption(declaredName, T::class)
}