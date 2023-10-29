package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.LoadEvent
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.arrayOfSize
import io.github.freya022.botcommands.api.core.utils.isSubclassOfAny
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.resolvers.*
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.parameters.toResolverFactory
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.Event
import java.util.*
import kotlin.reflect.full.isSubclassOf

@BService
class ResolverContainer internal constructor(
    context: BContext,
    private val serviceContainer: ServiceContainer
) {
    private val logger = KotlinLogging.logger { }

    private val factories: MutableList<ParameterResolverFactory<*>> = Collections.synchronizedList(arrayOfSize(50))
    private val cache: MutableMap<ParameterWrapper, ParameterResolverFactory<*>> = Collections.synchronizedMap(hashMapOf())

    init {
        context.getInterfacedServices<ParameterResolver<*, *>>().forEach(::addResolver)
        context.getInterfacedServices<ParameterResolverFactory<*>>().forEach(::addResolverFactory)
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

    fun addResolverFactory(resolver: ParameterResolverFactory<*>) {
        factories += resolver
        cache.clear()
    }

    @JvmSynthetic
    @BEventListener
    internal fun onLoad(event: LoadEvent) {
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

    @JvmSynthetic
    internal fun getResolverFactoryOrNull(parameter: ParameterWrapper): ParameterResolverFactory<*>? {
        val resolvableFactories = factories.filter { it.isResolvable(parameter) }
        check(resolvableFactories.size <= 1) {
            val factoryNameList = resolvableFactories.joinAsList { it.resolverType.simpleNestedName }
            "Found multiple compatible resolvers for parameter of type ${parameter.type.simpleNestedName}\n$factoryNameList"
        }

        return resolvableFactories.firstOrNull()
    }

    @JvmSynthetic
    internal inline fun <reified T : Any> hasResolverOfType(parameter: ParameterWrapper): Boolean {
        val resolverFactory = getResolverFactoryOrNull(parameter) ?: return false
        return resolverFactory.resolverType.isSubclassOf(T::class)
    }

    @JvmSynthetic
    internal fun getResolver(parameter: ParameterWrapper): ParameterResolver<*, *> {
        return cache.computeIfAbsent(parameter) { wrapper ->
            getResolverFactoryOrNull(wrapper) ?: run {
                val erasure = wrapper.erasure
                val serviceResult = serviceContainer.tryGetService(erasure)

                serviceResult.serviceError?.let { serviceError ->
                    wrapper.throwUser("Parameter #${wrapper.index} of type '${wrapper.type.simpleNestedName}' and name '${wrapper.name}' does not have any compatible resolver and service loading failed:\n${serviceError.toSimpleString()}")
                }

                ServiceCustomResolver(serviceResult.getOrThrow()).toResolverFactory()
            }
        }.get(parameter)
    }

    private fun hasCompatibleInterface(resolver: ParameterResolver<*, *>): Boolean {
        return resolver::class.isSubclassOfAny(compatibleInterfaces)
    }

    private class ServiceCustomResolver(private val o: Any) : ClassParameterResolver<ServiceCustomResolver, Any>(Any::class),
        ICustomResolver<ServiceCustomResolver, Any> {
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
            ICustomResolver::class
        )
    }
}