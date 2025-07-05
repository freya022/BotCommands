package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.javaType

private class ClassParameterResolverFactoryAdapter<T : ClassParameterResolver<out T, *>>(
    private val resolver: T,
    override val priority: Int,
): ParameterResolverFactory<T>(resolver::class) {
    override val supportedTypesStr: List<String> = listOf(resolver.jvmErasure.shortQualifiedName)

    override fun isResolvable(request: ResolverRequest): Boolean = resolver.jvmErasure == request.parameter.erasure
    override fun get(request: ResolverRequest): T = resolver
    override fun toString(): String = "ClassParameterResolverFactoryAdapter(resolver=$resolver)"
}

internal fun <T : ClassParameterResolver<out T, *>> T.toResolverFactory(annotation: Resolver): ParameterResolverFactory<T> {
    return ClassParameterResolverFactoryAdapter(this, annotation.priority)
}

private class TypedParameterResolverFactoryAdapter<T : TypedParameterResolver<out T, *>>(
    private val resolver: T,
    override val priority: Int,
): ParameterResolverFactory<T>(resolver::class) {
    override val supportedTypesStr: List<String> = listOf(resolver.type.shortQualifiedName)

    override fun isResolvable(request: ResolverRequest): Boolean {
        val requestedType = request.parameter.type
        return resolver.type == requestedType
                // Resolver of type T can resolve parameters of type T?
                || resolver.type == requestedType.withNullability(false)
                // Improves Java interoperability
                // Prevents issues when the resolver is for a k.c.List and Java parameter is a j.u.List
                // KType#javaType may have a few unsupported cases (it uses KType#stdlibJavaType),
                // while I believe it won't affect anyone,
                // it's still used as a last resort, just in case
                || resolver.type.javaType == requestedType.javaType
    }

    override fun get(request: ResolverRequest): T = resolver
    override fun toString(): String = "TypedParameterResolverFactoryAdapter(resolver=$resolver)"
}

internal fun <T : TypedParameterResolver<out T, *>> T.toResolverFactory(annotation: Resolver): ParameterResolverFactory<T> {
    return TypedParameterResolverFactoryAdapter(this, annotation.priority)
}