package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.reflect.throwUser
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.findAnnotationOnService
import io.github.freya022.botcommands.api.core.service.getServiceNamesForAnnotation
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.parameters.*
import io.github.freya022.botcommands.api.parameters.resolvers.*
import io.github.freya022.botcommands.internal.parameters.resolvers.annotations.ResolverMarker
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwInternal
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
    serviceContainer: ServiceContainer,
    resolverFactories: List<ParameterResolverFactory<*>>,
) {
    private data class CacheKey(
        private val requestedType: KClass<out IParameterResolver<*>>,
        private val resolverRequest: ResolverRequest
    )

    private val factories: MutableList<ParameterResolverFactory<*>> = arrayOfSize(50)
    private val cache: MutableMap<CacheKey, ParameterResolverFactory<*>?> = hashMapOf()

    init {
        fun addResolver(resolver: ParameterResolver<*, *>, annotation: Resolver) {
            fun ParameterResolver<*, *>.hasCompatibleInterface(): Boolean {
                return compatibleInterfaces.any { it.isInstance(this) }
            }

            require(resolver.hasCompatibleInterface()) {
                "The resolver should implement at least one of these interfaces: ${compatibleInterfaces.joinToString { it.simpleName!! }}"
            }

            factories += when (resolver) {
                is ClassParameterResolver -> resolver.toResolverFactory(annotation)
                is TypedParameterResolver -> resolver.toResolverFactory(annotation)
            }
        }

        // Add resolvers with their annotation
        serviceContainer.getServiceNamesForAnnotation<Resolver>().forEach { resolverName ->
            val annotation = serviceContainer.findAnnotationOnService<Resolver>(resolverName)
                ?: throwInternal("DI said ${annotationRef<Resolver>()} was present but isn't")
            val resolver = serviceContainer.getService(resolverName, ParameterResolver::class)
            addResolver(resolver, annotation)
        }

        factories += resolverFactories.also(::validateFactories)

        logger.trace {
            val factoriesByResolverType = hashMapOf<Class<*>, MutableList<ParameterResolverFactory<*>>>()
            for (factory in factories) {
                for (supportedResolver in factory.supportedResolvers) {
                    factoriesByResolverType.computeIfAbsent(supportedResolver) { arrayListOf() }.add(factory)
                }
            }

            val resolversStr = factoriesByResolverType.entries.joinToString("\n") { (interfaceClass, factories) ->
                buildString {
                    val factories = factories.sortedBy { it.factoryTypeOrAdaptedResolverType.simpleNestedName }

                    appendLine("${interfaceClass.simpleNestedName} (${factories.size}):")
                    append(factories.joinAsList(linePrefix = "\t-") { it.toLogString() })
                }
            }

            "Found resolvers:\n$resolversStr"
        }
    }

    private fun validateFactories(factories: List<ParameterResolverFactory<*>>) {
        // TODO test this
        // Check resolver factories are supporting the right interfaces
        for (factory in factories) {
            for (clazz in factory.supportedResolvers) {
                require(clazz.isSubclassOf<IParameterResolver<*>>() && ResolverMarker::class.java in clazz.interfaces) {
                    "${factory.toLogString()} declares supporting ${clazz.shortQualifiedName}, but it is not a built-in resolver"
                }
            }
        }
    }

    private val ParameterResolverFactory<*>.factoryTypeOrAdaptedResolverType: Class<*>
        get() = when (this) {
            is ParameterResolverFactoryAdapter -> this.resolverType.java
            else -> this.javaClass
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
            val factoryNameList = resolvableFactories.joinAsList { it.toLogString() }
            "Found multiple compatible resolvers, with the same priority\n$factoryNameList\nIncrease the priority of a resolver to override others"
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
            wrapper.throwUser("No ${resolverType.simpleNestedName} found for parameter '${wrapper.name}: ${wrapper.type.shortQualifiedName}'")
        }

        return factory.get(request)
    }

    internal fun clearCache() {
        cache.clear()
    }
}
