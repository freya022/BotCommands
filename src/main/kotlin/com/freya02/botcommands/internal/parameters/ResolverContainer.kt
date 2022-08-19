package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.core.events.LoadEvent
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

@BService
internal class ResolverContainer( //TODO Should be part of the base module
    classPathContainer: ClassPathContainer,
    private val serviceContainer: ServiceContainer
) {
    private val logger = Logging.getLogger()

    private val map: MutableMap<KClass<*>, Any> = hashMapOf()

    init {
        classPathContainer
            .classes
            .filter { it.isSubclassOf(ParameterResolver::class) }
            .forEach { clazz -> addResolver(serviceContainer.getService(clazz) as ParameterResolver<*, *>) }
    }

    fun addResolver(resolver: ParameterResolver<*, *>) {
        if (!hasCompatibleInterface(resolver)) {
            throwUser("The resolver should implement at least one of these interfaces: ${compatibleInterfaces.joinToString { it.simpleName!! }}")
        }

        map[resolver.jvmErasure]?.let { throwUser("Resolver for ${resolver.jvmErasure.qualifiedName} already exists") }

        map[resolver.jvmErasure] = resolver
    }

    @BEventListener
    fun onLoad(event: LoadEvent) = runInitialization {
        logger.debug("ResolverContainer loaded")
        if (map.isEmpty()) {
            logger.trace("Found no resolvers")
        } else {
            val resolversStr = compatibleInterfaces.joinToString("\n") { interfaceClass ->
                buildString {
                    val entriesOfType = map.entries
                        .filter { it.value::class.isSubclassOf(interfaceClass) }
                        .sortedBy { (type, _) -> type.simpleName }

                    append(interfaceClass.simpleName).append(" (${entriesOfType.size}):\n")
                    append(entriesOfType.joinToString("\n") { (type, resolver) ->
                        "\t- ${resolver::class.simpleName} (${type.simpleName})"
                    })
                }
            }

            logger.trace("Found resolvers:\n$resolversStr")
        }
    }

    fun getResolver(parameter: KParameter): Any {
        val requestedType = parameter.type.jvmErasure

        return map.computeIfAbsent(requestedType) { type ->
            val serviceResult = serviceContainer.tryGetService(type)

            serviceResult.onFailure {
                rethrowUser(
                    parameter.function,
                    "Parameter #${parameter.index} of type '${type.simpleName}' and name '${parameter.bestName}' does not have any compatible resolver and service loading failed",
                    it
                )
            }

            ServiceCustomResolver(serviceResult.getOrThrow())
        }
    }

    private fun hasCompatibleInterface(resolver: ParameterResolver<*, *>): Boolean {
        return resolver::class.isSubclassOfAny(compatibleInterfaces)
    }

    private class ServiceCustomResolver(private val o: Any) : ParameterResolver<ServiceCustomResolver, Any>(Any::class), ICustomResolver<ServiceCustomResolver, Any> {
        override suspend fun resolveSuspend(context: BContext, executableInteractionInfo: ExecutableInteractionInfo, event: Event) = o
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