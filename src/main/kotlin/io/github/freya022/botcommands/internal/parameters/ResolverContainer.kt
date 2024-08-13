package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.reflect.throwUser
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.arrayOfSize
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.*
import io.github.freya022.botcommands.api.parameters.resolvers.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger { }
private val compatibleInterfaces = listOf(
    TextParameterResolver::class,
    QuotableTextParameterResolver::class,
    SlashParameterResolver::class,
    ComponentParameterResolver::class,
    UserContextParameterResolver::class,
    MessageContextParameterResolver::class,
    ModalParameterResolver::class,
    TimeoutParameterResolver::class,
    ICustomResolver::class
)

@BService
internal class ResolverContainer internal constructor(
    resolvers: List<ParameterResolver<*, *>>,
    resolverFactories: List<ParameterResolverFactory<*>>,
) {
    private data class CacheKey(
        private val requestedType: KClass<out IParameterResolver<*>>,
        private val resolverRequest: ResolverRequest
    )

    private val factories: MutableList<ParameterResolverFactory<*>> = arrayOfSize(50)
    private val cache: MutableMap<CacheKey, ParameterResolverFactory<*>?> = hashMapOf()

    init {
        fun addResolver(resolver: ParameterResolver<*, *>) {
            fun ParameterResolver<*, *>.hasCompatibleInterface(): Boolean {
                return compatibleInterfaces.any { it.isInstance(this) }
            }

            require(resolver.hasCompatibleInterface()) {
                "The resolver should implement at least one of these interfaces: ${compatibleInterfaces.joinToString { it.simpleName!! }}"
            }

            factories += when (resolver) {
                is ClassParameterResolver -> resolver.toResolverFactory()
                is TypedParameterResolver -> resolver.toResolverFactory()
            }
        }

        resolvers.forEach(::addResolver)
        factories += resolverFactories

        logger.trace {
            val resolversStr = compatibleInterfaces.joinToString("\n") { interfaceClass ->
                buildString {
                    val factories = factories
                        .filter { factory -> factory.resolverType.isSubclassOf(interfaceClass) }
                        .sortedBy { it.resolverType.simpleNestedName }

                    appendLine("${interfaceClass.simpleNestedName} (${factories.size}):")
                    append(factories.joinAsList(linePrefix = "\t-") { "${it.resolverType.simpleNestedName} ; priority ${it.priority} (${it.supportedTypesStr.joinToString()})" })
                }
            }

            "Found resolvers:\n$resolversStr"
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : IParameterResolver<T>> getResolverFactoryOrNull(resolverType: KClass<out T>, request: ResolverRequest): ParameterResolverFactory<T>? {
        val key = CacheKey(resolverType, request)
        cache[key]?.let { return it as ParameterResolverFactory<T>? }

        val resolvableFactories = factories
            .filter { it.resolverType.isSubclassOf(resolverType) }
            .map { it as ParameterResolverFactory<T> }
            .filter { it.isResolvable(request) }
            .let { resolvableFactories ->
                if (resolvableFactories.isEmpty())
                    return@let resolvableFactories
                // Keep most important factories, if two has same priority, it gets reported down
                val maxPriority = resolvableFactories.maxOf { it.priority }
                resolvableFactories.filter { it.priority == maxPriority }
            }
        require(resolvableFactories.size <= 1) {
            val factoryNameList = resolvableFactories.joinAsList { it.resolverType.simpleNestedName }
            "Found multiple compatible resolvers, with the same priority, for the provided request\n$factoryNameList"
        }

        val factory = resolvableFactories.firstOrNull()
        cache[key] = factory
        return factory
    }

    internal inline fun <reified T : IParameterResolver<T>> hasResolverOfType(parameter: ParameterWrapper): Boolean {
        return hasResolverOfType<T>(ResolverRequest(parameter))
    }

    internal inline fun <reified T : IParameterResolver<T>> hasResolverOfType(request: ResolverRequest): Boolean {
        return getResolverFactoryOrNull(T::class, request) != null
    }

    internal inline fun <reified T : IParameterResolver<T>> getResolverOfType(request: ResolverRequest): T {
        return getResolver(T::class, request)
    }

    internal fun <T : IParameterResolver<T>> getResolver(resolverType: KClass<T>, request: ResolverRequest): T {
        val factory = getResolverFactoryOrNull(resolverType, request)
        if (factory == null) {
            val wrapper = request.parameter
            wrapper.throwUser("No ${resolverType.simpleNestedName} found for parameter '${wrapper.name}: ${wrapper.type.simpleNestedName}'")
        }

        return factory.get(request)
    }
}