package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

/**
 * Factory for [parameter resolvers][ParameterResolver].
 *
 * @see ParameterResolver
 */
@InterfacedService(acceptMultiple = true)
abstract class ParameterResolverFactory<T : ParameterResolver<out T, *>>(val resolverType: KClass<out T>) {
    constructor(resolverType: Class<T>) : this(resolverType.kotlin)

    /**
     * List of types as strings that are supported by this resolver factory.
     *
     * This should be the types of what the returned parameter resolvers are capable of returning.
     *
     * This is only used for logging purposes.
     */
    abstract val supportedTypesStr: List<String>

    abstract fun isResolvable(parameter: ParameterWrapper): Boolean

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