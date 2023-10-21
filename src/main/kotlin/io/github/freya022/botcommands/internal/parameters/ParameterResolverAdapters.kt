package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver

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