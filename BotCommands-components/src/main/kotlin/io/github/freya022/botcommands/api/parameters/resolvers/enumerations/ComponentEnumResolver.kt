package io.github.freya022.botcommands.api.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.ComponentEnumResolverImpl
import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.EnumResolverBuilderImpl
import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.EnumResolverModuleMixin

/**
 * Entry point to create enum resolver modules for components.
 */
object ComponentEnumResolver {
    /**
     * Creates a new enum resolver module for components.
     *
     * It must be registered using [EnumResolverBuilder.with].
     */
    @JvmStatic
    fun <E : Enum<E>> of(enumType: Class<E>): EnumResolverModule<E> {
        return object : EnumResolverModuleMixin<E> {
            override fun createResolver(base: EnumResolverBuilderImpl.AsImmutable<E>): ClassParameterResolver<*, E> {
                return ComponentEnumResolverImpl(enumType)
            }
        }
    }
}

/**
 * Register support for component parameters.
 */
inline fun <reified E : Enum<E>> EnumResolverBuilder<E>.withComponents(): EnumResolverBuilder<E> {
    return with(ComponentEnumResolver.of(E::class.java))
}
