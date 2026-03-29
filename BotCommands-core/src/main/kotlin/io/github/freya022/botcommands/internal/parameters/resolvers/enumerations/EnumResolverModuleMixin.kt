package io.github.freya022.botcommands.internal.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumResolverModule

// If shared internals existed, we could move the function to the API interface, change it to an abstract class and add synthetic flag
interface EnumResolverModuleMixin<E : Enum<E>> : EnumResolverModule<E> {
    fun createResolver(base: EnumResolverBuilderImpl.AsImmutable<E>): ClassParameterResolver<*, E>
}
