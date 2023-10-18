package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlin.reflect.KType

/**
 * Base class for parameter resolvers using static types.
 *
 * This is best suited if you want a simple resolver for a specific type, such as `[Map]<Int, String]>` for example.
 *
 * @see ClassParameterResolver
 *
 * @see TypedParameterResolverFactory
 */
@InterfacedService(acceptMultiple = true)
abstract class TypedParameterResolver<T : TypedParameterResolver<T, R>, R : Any>(
    val type: KType
) : ParameterResolver<T, R>() {
    override fun toString(): String {
        return "TypedParameterResolver(type=$type)"
    }
}