package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.LoadEvent
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.reflect.throwUser
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.parameters.resolvers.*
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.core.service.tryGetWrappedService
import io.github.freya022.botcommands.internal.parameters.toResolverFactory
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isNullable
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.Event
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

@BService
class ResolverContainer internal constructor(
    resolvers: List<ParameterResolver<*, *>>,
    resolverFactories: List<ParameterResolverFactory<*>>,
    private val serviceContainer: ServiceContainer
) {
    private data class CacheKey(
        private val requestedType: KClass<out IParameterResolver<*>>,
        private val resolverRequest: ResolverRequest
    )

    private val logger = KotlinLogging.logger { }

    private val lock = ReentrantLock()
    private val factories: MutableList<ParameterResolverFactory<*>> = arrayOfSize(50)
    private val cache: MutableMap<CacheKey, ParameterResolverFactory<*>?> = hashMapOf()

    init {
        resolvers.forEach(::addResolver)
        resolverFactories.forEach(::addResolverFactory)
    }

    fun addResolver(resolver: ParameterResolver<*, *>) {
        if (!hasCompatibleInterface(resolver)) {
            throwUser("The resolver should implement at least one of these interfaces: ${compatibleInterfaces.joinToString { it.simpleName!! }}")
        }

        when (resolver) {
            is ClassParameterResolver -> addResolverFactory(resolver.toResolverFactory())
            is TypedParameterResolver -> addResolverFactory(resolver.toResolverFactory())
        }
    }

    fun addResolverFactory(resolver: ParameterResolverFactory<*>) = lock.withLock {
        factories += resolver
        cache.clear()
    }

    @JvmSynthetic
    @BEventListener
    internal fun onLoad(event: LoadEvent) = lock.withLock {
        if (factories.isEmpty()) {
            throwInternal("No resolvers/factories were found") //Never happens
        } else {
            logger.trace {
                val resolversStr = compatibleInterfaces.joinToString("\n") { interfaceClass ->
                    buildString {
                        val factories = factories
                            .filter { factory -> factory.resolverType.isSubclassOf(interfaceClass) }
                            .sortedBy { it.resolverType.simpleNestedName }

                        appendLine("${interfaceClass.simpleNestedName} (${factories.size}):")
                        append(factories.joinAsList(linePrefix = "\t-") { "${it.resolverType.simpleNestedName} (${it.supportedTypesStr.joinToString()})" })
                    }
                }

                "Found resolvers:\n$resolversStr"
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    internal fun <T : IParameterResolver<T>> getResolverFactoryOrNull(resolverType: KClass<out T>, request: ResolverRequest): ParameterResolverFactory<T>? = lock.withLock {
        val key = CacheKey(resolverType, request)
        cache[key]?.let { return@withLock it as ParameterResolverFactory<T>? }

        val resolvableFactories = factories
            .filter { it.resolverType.isSubclassOf(resolverType) }
            .map { it as ParameterResolverFactory<T> }
            .filter { it.isResolvable(request) }
        require(resolvableFactories.size <= 1) {
            val factoryNameList = resolvableFactories.joinAsList { it.resolverType.simpleNestedName }
            "Found multiple compatible resolvers for the provided request\n$factoryNameList"
        }

        val factory = getFactoryOrServiceFactory(resolverType, resolvableFactories, request) as ParameterResolverFactory<T>?
        cache[key] = factory
        factory
    }

    private fun <T : IParameterResolver<T>> getFactoryOrServiceFactory(
        resolverType: KClass<out IParameterResolver<*>>,
        resolvableFactories: List<ParameterResolverFactory<T>>,
        request: ResolverRequest
    ): ParameterResolverFactory<*>? {
        if (resolvableFactories.isNotEmpty()) {
            return resolvableFactories.first()
        } else if (resolverType.isSubclassOf(ICustomResolver::class)) {
            val wrapper = request.parameter
            val serviceResult = serviceContainer.tryGetWrappedService(wrapper.parameter)
            val serviceError = serviceResult.serviceError
            if (serviceError == null) {
                return serviceResult.getOrThrow().let(::ServiceCustomResolver).toResolverFactory()
            } else if (wrapper.parameter.isNullable || wrapper.parameter.isOptional) {
                // If the parameter is nullable/optional, give a resolver that returns null
                logger.trace { "No ${resolverType.simpleNestedName} found for parameter '${wrapper.name}: ${wrapper.type.simpleNestedName}' and service loading failed:\n${serviceError.toDetailedString()}" }
                return NullServiceCustomResolverFactory
            }
        }

        return null
    }

    @JvmSynthetic
    internal inline fun <reified T : IParameterResolver<T>> hasResolverOfType(parameter: ParameterWrapper): Boolean {
        return hasResolverOfType<T>(ResolverRequest(parameter))
    }

    @JvmSynthetic
    internal inline fun <reified T : IParameterResolver<T>> hasResolverOfType(request: ResolverRequest): Boolean {
        return getResolverFactoryOrNull(T::class, request) != null
    }

    @JvmSynthetic
    internal inline fun <reified T : IParameterResolver<T>> getResolverOfType(request: ResolverRequest): T {
        return getResolver(T::class, request)
    }

    @JvmSynthetic
    internal fun <T : IParameterResolver<T>> getResolver(resolverType: KClass<T>, request: ResolverRequest): T {
        val factory = getResolverFactoryOrNull(resolverType, request)
        if (factory == null) {
            val wrapper = request.parameter
            // Custom resolvers are often used for services
            // if not one, make a simple error
            if (!resolverType.isSubclassOf(ICustomResolver::class))
                wrapper.throwUser("No ${resolverType.simpleNestedName} found for parameter '${wrapper.name}: ${wrapper.type.simpleNestedName}'.")

            // If a service factory, add the error
            val serviceError = serviceContainer.tryGetWrappedService(wrapper.parameter).serviceError ?: throwInternal("Service became available after failing")
            wrapper.throwUser("No ${resolverType.simpleNestedName} found for parameter '${wrapper.name}: ${wrapper.type.simpleNestedName}' and service loading failed:\n${serviceError.toDetailedString()}")
        }

        return factory.get(request)
    }

    private fun hasCompatibleInterface(resolver: ParameterResolver<*, *>): Boolean {
        return resolver::class.isSubclassOfAny(compatibleInterfaces)
    }

    private data object NullServiceCustomResolverFactory : TypedParameterResolverFactory<NullServiceCustomResolverFactory.NullServiceCustomResolver>(NullServiceCustomResolver::class, Any::class) {
        private data object NullServiceCustomResolver : ClassParameterResolver<NullServiceCustomResolver, Any>(Any::class), ICustomResolver<NullServiceCustomResolver, Any> {
            override suspend fun resolveSuspend(info: IExecutableInteractionInfo, event: Event): Any? = null
        }

        override fun get(request: ResolverRequest): NullServiceCustomResolver = NullServiceCustomResolver
    }

    private class ServiceCustomResolver<T : Any>(
        private val o: T
    ) : ClassParameterResolver<ServiceCustomResolver<T>, T>(o::class),
        ICustomResolver<ServiceCustomResolver<T>, T> {

        override suspend fun resolveSuspend(info: IExecutableInteractionInfo, event: Event) = o
    }

    internal companion object {
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
    }
}