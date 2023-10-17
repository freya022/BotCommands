package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
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

    abstract fun isResolvable(type: KType): Boolean

    abstract fun get(parameter: ParameterWrapper): T

    override fun toString(): String {
        return "ParameterResolverFactory(resolverType=${resolverType.shortQualifiedName})"
    }
}

private class ClassParameterResolverFactoryAdapter<T : ClassParameterResolver<out T, *>>(
    private val resolver: T
): ParameterResolverFactory<T>(resolver::class) {
    override fun isResolvable(type: KType): Boolean = resolver.jvmErasure == type.jvmErasure
    override fun get(parameter: ParameterWrapper): T = resolver
    override fun toString(): String = "ClassParameterResolverFactoryAdapter(resolver=$resolver)"
}

internal fun <T : ClassParameterResolver<out T, *>> T.toResolverFactory(): ParameterResolverFactory<T> {
    return ClassParameterResolverFactoryAdapter(this)
}

private class TypedParameterResolverFactoryAdapter<T : TypedParameterResolver<out T, *>>(
    private val resolver: T
): ParameterResolverFactory<T>(resolver::class) {
    override fun isResolvable(type: KType): Boolean = resolver.type == type
    override fun get(parameter: ParameterWrapper): T = resolver
    override fun toString(): String = "TypedParameterResolverFactoryAdapter(resolver=$resolver)"
}

internal fun <T : TypedParameterResolver<out T, *>> T.toResolverFactory(): ParameterResolverFactory<T> {
    return TypedParameterResolverFactoryAdapter(this)
}