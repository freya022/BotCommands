package com.freya02.botcommands.commands.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.core.internal.events.LoadEvent
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

@BService
internal class ResolverContainer( //TODO Should be part of the base module
    classPathContainer: ClassPathContainer,
    private val serviceContainer: ServiceContainer
) {
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

        map[resolver.type.jvmErasure]?.let { throwUser("Resolver for ${resolver.type.jvmErasure.qualifiedName} already exists") }

        map[resolver.type.jvmErasure] = resolver
    }

    @BEventListener
    fun onLoad(event: LoadEvent) = runInitialization {
        LOGGER.debug("ResolverContainer loaded")
        if (map.isEmpty()) {
            LOGGER.trace("Found no resolvers")
        } else {
            val resolversStr = compatibleInterfaces.joinToString("\n") { interfaceClass ->
                buildString {
                    append(interfaceClass.simpleName).append(":\n")
                    append(map.entries.filter { it.value::class.isSubclassOf(interfaceClass) }.joinToString("\n") { (type, resolver) ->
                        "\t- ${resolver::class.simpleName} (${type.simpleName})"
                    })
                }
            }

            LOGGER.trace("Found resolvers:\n$resolversStr")
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
        return resolver::class.isSubclassOfAny(*compatibleInterfaces.toTypedArray())
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