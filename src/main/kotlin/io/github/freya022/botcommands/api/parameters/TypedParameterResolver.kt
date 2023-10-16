package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlin.reflect.KType

/**
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