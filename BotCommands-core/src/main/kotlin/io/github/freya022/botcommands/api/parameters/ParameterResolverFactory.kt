package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.allInterfaces
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverMarker
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
 * If multiple factories return `true` in [isResolvable] for a given type,
 * the factory with the best [priority] is taken, if two with the same top priority exists, an exception is thrown.
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
abstract class ParameterResolverFactory {

    /**
     * List of types as strings that are supported by this resolver factory.
     *
     * This should be the types of what the returned parameter resolvers are capable of returning.
     *
     * This is only used for logging purposes.
     */
    abstract val supportedTypesStr: List<String>

    /**
     * List of resolvers supported by this resolver factory.
     *
     * These should only be types directly extending `BuiltinResolver` (an internal interface).
     */
    abstract val supportedResolvers: List<Class<out IParameterResolver<*>>>

    /**
     * The priority of this factory.
     *
     * When getting a resolver factory, the factory with the highest value that is [resolvable][isResolvable] is taken.
     *
     * If two factories with the same priority exist and are both resolvable, an exception is thrown.
     *
     * @see Resolver.priority
     */
    open val priority: Int get() = 0

    /**
     * Determines if a given parameter is supported.
     *
     * Out of all factories supporting the given [ResolverRequest.resolverType], and with the same [priority], only one must return `true`.
     *
     * This only runs for resolvers declared as supported by [supportedResolvers].
     */
    abstract fun isResolvable(request: ResolverRequest): Boolean

    /**
     * Returns a [ParameterResolver] for the given parameter.
     *
     * This is only called if [isResolvable] returned `true`.
     */
    abstract fun get(request: ResolverRequest): IParameterResolver<*>

    override fun toString(): String {
        return "ParameterResolverFactory(supportedResolvers=${supportedResolvers.map { it.simpleNestedName }}, supportedTypes=${supportedTypesStr})"
    }

    open fun toLogString(): String {
        return "${this.javaClass.shortQualifiedName} ; priority $priority (${supportedTypesStr.joinToString()})"
    }

    companion object {
        // TODO docs
        @JvmStatic
        fun inferSupportedResolversFrom(resolverType: Class<out IParameterResolver<*>>): List<Class<out IParameterResolver<*>>> {
            @Suppress("UNCHECKED_CAST")
            return resolverType
                .allInterfaces
                .filter { it.isSubclassOf<IParameterResolver<*>>() && ResolverMarker::class.java in it.interfaces }
                    as List<Class<out IParameterResolver<*>>>
        }

        @JvmSynthetic
        inline fun <reified T : IParameterResolver<*>> inferSupportedResolversFrom(): List<Class<out IParameterResolver<*>>> {
            return inferSupportedResolversFrom(T::class.java)
        }
    }
}
