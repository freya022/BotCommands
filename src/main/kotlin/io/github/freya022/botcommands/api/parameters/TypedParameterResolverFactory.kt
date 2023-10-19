package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

/**
 * Specialization of [ParameterResolverFactory] for a specific [KType].
 *
 * Your implementation needs to be annotated with [@ResolverFactory][ResolverFactory].
 *
 * @see ParameterResolverFactory
 *
 * @param T Type of the returned parameter resolver
 */
abstract class TypedParameterResolverFactory<T : ParameterResolver<T, *>>(
    resolverType: KClass<out T>,
    protected val type: KType
) : ParameterResolverFactory<T>(resolverType) {
    override val supportedTypesStr: List<String> = listOf(type.simpleNestedName)

    constructor(resolverType: Class<T>, type: Class<*>) : this(resolverType.kotlin, type.kotlin.starProjectedType)

    override fun isResolvable(parameter: ParameterWrapper): Boolean {
        return this.type == parameter.type
    }
}