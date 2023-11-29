package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

/**
 * Specialization of [ParameterResolverFactory] for a specific [KType].
 *
 * Your implementation needs to be annotated with [@ResolverFactory][ResolverFactory].
 *
 * @see ParameterResolverFactory
 *
 * @param resolverType Class of the returned parameter resolver
 * @param type         Type of the objects returned by the parameter resolver
 * @param T            Type of the returned parameter resolver
 */
abstract class TypedParameterResolverFactory<T : ParameterResolver<T, *>>(
    resolverType: KClass<out T>,
    protected val type: KType
) : ParameterResolverFactory<T>(resolverType) {
    override val supportedTypesStr: List<String> = listOf(type.simpleNestedName)

    constructor(resolverType: KClass<T>, type: KClass<*>) : this(resolverType, type.starProjectedType)
    constructor(resolverType: Class<T>, type: Class<*>) : this(resolverType.kotlin, type.kotlin.starProjectedType)

    init {
        check(!type.isMarkedNullable) {
            "Typed parameter resolver factories cannot have a nullable type"
        }
    }

    override fun isResolvable(parameter: ParameterWrapper): Boolean {
        return this.type == parameter.type || this.type == parameter.type.withNullability(false)
    }
}