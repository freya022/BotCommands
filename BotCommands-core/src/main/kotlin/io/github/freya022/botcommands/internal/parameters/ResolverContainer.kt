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
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverProvider
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverManagerImpl
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverMarker
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
class ResolverContainer(
    serviceContainer: ServiceContainer,
    resolverFactories: List<ParameterResolverFactory>,
    resolverProviders: List<ResolverProvider>,
) {
    private val factories: MutableList<ParameterResolverFactory> = arrayOfSize(50)
    private val cache: MutableMap<ResolverRequest, ParameterResolverFactory?> = hashMapOf()

    init {
        factories += resolverFactories.also(::validateFactories)
        factories += createWrappedResolvers(serviceContainer)

        val resolverManager = ResolverManagerImpl()
        for (provider in resolverProviders) {
            provider.declare(resolverManager)
        }
        factories += resolverManager.resolverFactories.also(::validateFactories)

        logger.trace {
            val factoriesByResolverType = hashMapOf<Class<*>, MutableList<ParameterResolverFactory>>()
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

    private fun validateFactories(factories: List<ParameterResolverFactory>) {
        // Check resolver factories are supporting the right interfaces
        for (factory in factories) {
            for (clazz in factory.supportedResolvers) {
                require(clazz.isSubclassOf<IParameterResolver<*>>() && ResolverMarker::class.java in clazz.interfaces) {
                    "${factory.toLogString()} declares supporting ${clazz.shortQualifiedName}, but it is not a built-in resolver"
                }
            }
        }
    }

    private fun createWrappedResolvers(serviceContainer: ServiceContainer): List<ParameterResolverFactory> {
        return serviceContainer.getServiceNamesForAnnotation<Resolver>().map { resolverName ->
            val annotation = serviceContainer.findAnnotationOnService<Resolver>(resolverName)
                ?: throwInternal("DI said ${annotationRef<Resolver>()} was present but isn't")
            when (val resolver = serviceContainer.getService(resolverName, ParameterResolver::class)) {
                is ClassParameterResolver -> resolver.toResolverFactory(annotation)
                is TypedParameterResolver -> resolver.toResolverFactory(annotation)
            }
        }
    }

    private val ParameterResolverFactory.factoryTypeOrAdaptedResolverType: Class<*>
        get() = when (this) {
            is ParameterResolverFactoryAdapter -> this.resolverType
            else -> this.javaClass
        }

    internal fun getResolverFactoryOrNull(request: ResolverRequest): ParameterResolverFactory? {
        cache[request]?.let { return it as ParameterResolverFactory? }

        val resolvableFactories = factories
            .filter { it.supportedResolvers.any { supportedResolver -> supportedResolver == request.resolverType } }
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
        cache[request] = factory
        return factory
    }

    inline fun <reified T : IParameterResolver<T>> hasResolverOfType(parameter: ParameterWrapper): Boolean {
        return hasResolver(TypedResolverRequest(T::class.java, parameter))
    }

    fun hasResolver(request: ResolverRequest): Boolean {
        return getResolverFactoryOrNull(request) != null
    }

    @PublishedApi // Shared internal
    internal fun <T : IParameterResolver<T>> getResolverOfType(request: TypedResolverRequest<T>): T {
        return getResolver(request)
    }

    fun <T : IParameterResolver<T>> getResolver(request: TypedResolverRequest<T>): T {
        val resolverType = request.resolverType
        val factory = getResolverFactoryOrNull(request)
        if (factory == null) {
            val wrapper = request.parameter
            wrapper.throwUser("No ${resolverType.simpleNestedName} found for parameter '${wrapper.name}: ${wrapper.type.shortQualifiedName}'")
        }

        val resolver = factory.get(request)
        return if (resolverType.isInstance(resolver)) {
            resolverType.cast(resolver)
        } else {
            error("${factory.toLogString()} returned a resolver of type ${resolver.javaClass.shortQualifiedName} which does not support ${resolverType.shortQualifiedName}")
        }
    }

    internal fun clearCache() {
        cache.clear()
    }
}
