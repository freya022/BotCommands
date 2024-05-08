package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Factory for [parameter resolvers][ParameterResolver].
 *
 * Your implementation needs to be annotated with [@ResolverFactory][ResolverFactory].
 *
 * ### How it works
 *
 * A factory determines if a given parameter is supported, if so, a parameter resolver will be created.
 *
 * **Note:** If multiple factories return `true` in [isResolvable] for a given type, an exception is thrown.
 *
 * ### Use cases
 *
 * This is particularly useful if your parameter's type has generics, which you may read in [ParameterWrapper.type],
 * or if you need to read an annotation on the [parameter][ParameterWrapper.parameter]
 * to adjust the resolver's behavior, for example.
 *
 * In case you want to read generics, you can read them off the [KType] in [ParameterWrapper],
 * but you can only do that with Kotlin.
 *
 * In case you want to read the annotations, you can use the methods supplied by [ParameterWrapper].
 *
 * @param T Type of the returned parameter resolver
 *
 * @see TypedParameterResolverFactory
 * @see ParameterResolver
 */
@InterfacedService(acceptMultiple = true)
abstract class ParameterResolverFactory<T : IParameterResolver<T>>(val resolverType: KClass<out T>) {
    constructor(resolverType: Class<out T>) : this(resolverType.kotlin)

    /**
     * List of types as strings that are supported by this resolver factory.
     *
     * This should be the types of what the returned parameter resolvers are capable of returning.
     *
     * This is only used for logging purposes.
     */
    abstract val supportedTypesStr: List<String>

    /**
     * Determines if a given parameter is supported, only one factory must return `true`.
     *
     * This only gets called if the requested resolver is compatible with [resolverType].
     */
    abstract fun isResolvable(request: ResolverRequest): Boolean

    /**
     * Returns a [ParameterResolver] for the given parameter.
     *
     * This is only called if [isResolvable] returned `true`.
     */
    abstract fun get(request: ResolverRequest): T

    override fun toString(): String {
        return "ParameterResolverFactory(resolverType=${resolverType.shortQualifiedName})"
    }
}