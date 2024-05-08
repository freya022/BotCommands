package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

/**
 * Specialization of [ParameterResolverFactory] for a specific [KType].
 *
 * Your implementation needs to be annotated with [@ResolverFactory][ResolverFactory].
 *
 * You can also create a service factory using [resolverFactory].
 *
 * @see ParameterResolverFactory
 * @see resolverFactory
 *
 * @param resolverType Class of the returned parameter resolver
 * @param type         Type of the objects returned by the parameter resolver
 * @param T            Type of the returned parameter resolver
 */
abstract class TypedParameterResolverFactory<T : IParameterResolver<T>>(
    resolverType: KClass<out T>,
    val type: KType
) : ParameterResolverFactory<T>(resolverType) {
    override val supportedTypesStr: List<String> = listOf(type.simpleNestedName)

    constructor(resolverType: KClass<T>, type: KClass<*>) : this(resolverType, type.starProjectedType)
    constructor(resolverType: Class<T>, type: Class<*>) : this(resolverType.kotlin, type.kotlin.starProjectedType)

    init {
        require(!type.isMarkedNullable) {
            "Typed parameter resolver factories cannot have a nullable type"
        }
    }

    override fun isResolvable(request: ResolverRequest): Boolean {
        val requestedType = request.parameter.type
        return this.type == requestedType || this.type == requestedType.withNullability(false)
    }
}