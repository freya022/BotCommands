package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumResolverBuilder
import java.util.function.Consumer

/**
 * Management object used by [ResolverProvider].
 *
 * @see ResolverProvider
 */
interface ResolverManager {
    fun register(resolverFactory: ParameterResolverFactory)

    fun <E : Enum<E>> registerEnum(enumType: Class<E>, block: Consumer<EnumResolverBuilder<E>>)
}

inline fun <reified E : Enum<E>> ResolverManager.registerEnum(noinline block: EnumResolverBuilder<E>.() -> Unit) {
    registerEnum(E::class.java, block)
}
