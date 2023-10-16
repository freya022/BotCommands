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
abstract class ParameterResolverFactory<T : ParameterResolver<*, R>, R : Any>(val resolverType: KClass<out T>) {
    constructor(resolverType: Class<T>) : this(resolverType.kotlin)

    abstract fun isResolvable(type: KType): Boolean

    abstract fun get(parameter: ParameterWrapper): T

    override fun toString(): String {
        return "ParameterResolverFactory(resolverType=${resolverType.shortQualifiedName})"
    }

    companion object {
        fun <T : ClassParameterResolver<*, R>, R : Any> fromClassParameterResolver(resolver: T): ParameterResolverFactory<*, R> {
            return ClassParameterResolverFactoryAdapter(resolver)
        }
    }

    private class ClassParameterResolverFactoryAdapter<T : ClassParameterResolver<T, R>, R : Any>(
        private val resolver: T
    ): ParameterResolverFactory<T, R>(resolver::class) {
        override fun isResolvable(type: KType): Boolean {
            return resolver.jvmErasure == type.jvmErasure
        }

        override fun get(parameter: ParameterWrapper): T {
            return resolver
        }

        override fun toString(): String {
            return "ClassParameterResolverFactoryAdapter(resolver=$resolver)"
        }
    }
}
