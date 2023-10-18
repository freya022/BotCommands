package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Factory for [parameter resolvers][ParameterResolver].
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
 * @see TypedParameterResolverFactory
 * @see ParameterResolver
 */
@InterfacedService(acceptMultiple = true)
abstract class ParameterResolverFactory<T : ParameterResolver<out T, *>>(val resolverType: KClass<out T>) {
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
     * Determines if a given parameter is supported.
     *
     * Only one factory must return `true`.
     */
    abstract fun isResolvable(parameter: ParameterWrapper): Boolean

    /**
     * Returns a [ParameterResolver] for the given parameter.
     *
     * This is only called if [isResolvable] returned `true`.
     */
    abstract fun get(parameter: ParameterWrapper): T

    override fun toString(): String {
        return "ParameterResolverFactory(resolverType=${resolverType.shortQualifiedName})"
    }
}

private class ClassParameterResolverFactoryAdapter<T : ClassParameterResolver<out T, *>>(
    private val resolver: T
): ParameterResolverFactory<T>(resolver::class) {
    override val supportedTypesStr: List<String> = listOf(resolver.jvmErasure.simpleNestedName)

    override fun isResolvable(parameter: ParameterWrapper): Boolean = resolver.jvmErasure == parameter.erasure
    override fun get(parameter: ParameterWrapper): T = resolver
    override fun toString(): String = "ClassParameterResolverFactoryAdapter(resolver=$resolver)"
}

internal fun <T : ClassParameterResolver<out T, *>> T.toResolverFactory(): ParameterResolverFactory<T> {
    return ClassParameterResolverFactoryAdapter(this)
}

private class TypedParameterResolverFactoryAdapter<T : TypedParameterResolver<out T, *>>(
    private val resolver: T
): ParameterResolverFactory<T>(resolver::class) {
    override val supportedTypesStr: List<String> = listOf(resolver.type.simpleNestedName)

    override fun isResolvable(parameter: ParameterWrapper): Boolean = resolver.type == parameter.type
    override fun get(parameter: ParameterWrapper): T = resolver
    override fun toString(): String = "TypedParameterResolverFactoryAdapter(resolver=$resolver)"
}

internal fun <T : TypedParameterResolver<out T, *>> T.toResolverFactory(): ParameterResolverFactory<T> {
    return TypedParameterResolverFactoryAdapter(this)
}