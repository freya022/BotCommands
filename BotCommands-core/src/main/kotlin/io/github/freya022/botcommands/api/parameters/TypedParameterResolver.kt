package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.reflect.KotlinTypeToken
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import kotlin.reflect.KType

/**
 * Base class for parameter resolvers using static types.
 *
 * This is best suited if you want a simple resolver for a specific type, such as `List<MyValidatedObject>` for example.
 *
 * Your implementation needs to be annotated with [@Resolver][Resolver],
 * unless this is an output of a [ParameterResolverFactory].
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 *
 * @see ClassParameterResolver
 *
 * @see TypedParameterResolverFactory
 */
@InterfacedService(acceptMultiple = true)
abstract class TypedParameterResolver<T : TypedParameterResolver<T, R>, R : Any>(
    val type: KType
) : ParameterResolver<T, R>() {

    constructor(typeToken: KotlinTypeToken<R>) : this(typeToken.type)

    override fun toString(): String {
        return "TypedParameterResolver(type=$type)"
    }
}