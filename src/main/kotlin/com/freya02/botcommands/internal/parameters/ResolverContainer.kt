package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.LoadEvent
import com.freya02.botcommands.api.core.service.ServiceContainer
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.service.annotations.ResolverFactory
import com.freya02.botcommands.api.core.utils.isSubclassOfAny
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.utils.runInitialization
import com.freya02.botcommands.internal.utils.throwInternal
import com.freya02.botcommands.internal.utils.throwUser
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

@BService
internal class ResolverContainer(
    context: BContextImpl,
    private val serviceContainer: ServiceContainer
) {
    private val logger = KotlinLogging.logger { }

    private val factories: MutableMap<KClass<*>, ParameterResolverFactory<*, *>> = Collections.synchronizedMap(hashMapOf())

    init {
        context.instantiableServiceAnnotationsMap
            .getInstantiableClassesWithAnnotationAndType<Resolver, ParameterResolver<*, *>>()
            .forEach { clazz -> addResolver(serviceContainer.getService(clazz)) }

        context.instantiableServiceAnnotationsMap
            .getInstantiableClassesWithAnnotationAndType<ResolverFactory, ParameterResolverFactory<*, *>>()
            .forEach { clazz -> addResolverFactory(serviceContainer.getService(clazz)) }
    }

    fun <R : Any> addResolver(resolver: ParameterResolver<*, R>) {
        if (!hasCompatibleInterface(resolver)) {
            throwUser("The resolver should implement at least one of these interfaces: ${compatibleInterfaces.joinToString { it.simpleName!! }}")
        }

        addResolverFactory(ParameterResolverFactory.singleton(resolver))
    }

    fun <R : Any> addResolverFactory(resolver: ParameterResolverFactory<*, R>) {
        factories[resolver.jvmErasure]?.let { throwUser("Resolver for ${resolver.jvmErasure.qualifiedName} already exists") }

        factories[resolver.jvmErasure] = resolver
    }

    @BEventListener
    fun onLoad(event: LoadEvent) = runInitialization {
        if (factories.isEmpty()) {
            throwInternal("No resolvers/factories were found") //Never happens
        } else {
            logger.trace {
                val resolversStr = compatibleInterfaces.joinToString("\n") { interfaceClass ->
                    buildString {
                        val entriesOfType = factories
                            .mapValues { it.value.resolverType }
                            .filterValues { resolverType -> resolverType.isSubclassOf(interfaceClass) }
                            .entries
                            .sortedBy { (type, _) -> type.simpleName }

                        append(interfaceClass.simpleName).append(" (${entriesOfType.size}):\n")
                        append(entriesOfType.joinToString("\n") { (type, resolver) ->
                            "\t- ${resolver.simpleName} (${type.simpleName})"
                        })
                    }
                }

                "Found resolvers:\n$resolversStr"
            }
        }
    }

    @JvmSynthetic
    internal fun getResolverOrNull(parameter: KParameter) = factories[parameter.type.jvmErasure]

    fun getResolver(parameter: ParameterWrapper): ParameterResolver<*, *> {
        val requestedType = parameter.erasure

        return factories.computeIfAbsent(requestedType) { type ->
            val serviceResult = serviceContainer.tryGetService(type)

            serviceResult.serviceError?.let { serviceError ->
                parameter.throwUser("Parameter #${parameter.index} of type '${type.simpleName}' and name '${parameter.name}' does not have any compatible resolver and service loading failed: ${serviceError.toSimpleString()}")
            }

            ParameterResolverFactory.singleton(ServiceCustomResolver(serviceResult.getOrThrow()))
        }.get(parameter)
    }

    private fun hasCompatibleInterface(resolver: ParameterResolver<*, *>): Boolean {
        return resolver::class.isSubclassOfAny(compatibleInterfaces)
    }

    private class ServiceCustomResolver(private val o: Any) : ParameterResolver<ServiceCustomResolver, Any>(Any::class), ICustomResolver<ServiceCustomResolver, Any> {
        override suspend fun resolveSuspend(context: BContext, executableInteractionInfo: IExecutableInteractionInfo, event: Event) = o
    }

    companion object {
        private val compatibleInterfaces = listOf(
            RegexParameterResolver::class,
            SlashParameterResolver::class,
            ComponentParameterResolver::class,
            UserContextParameterResolver::class,
            MessageContextParameterResolver::class,
            ICustomResolver::class
        )
    }
}