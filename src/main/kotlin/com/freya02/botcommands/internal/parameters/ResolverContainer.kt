package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.ServiceContainer
import com.freya02.botcommands.internal.core.events.LoadEvent
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

@BService
internal class ResolverContainer(
    classPathContainer: ClassPathContainer,
    private val serviceContainer: ServiceContainer
) {
    private val logger = KotlinLogging.logger {  }

    private val map: MutableMap<KClass<*>, Any> = Collections.synchronizedMap(hashMapOf())

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

            logger.trace { "Found resolvers:\n$resolversStr" }
        }
    }

    @JvmSynthetic
    internal fun getResolverOrNull(parameter: KParameter) = map[parameter.type.jvmErasure]

    fun getResolver(parameter: KParameter): Any {
        val requestedType = parameter.type.jvmErasure

        return map.computeIfAbsent(requestedType) { type ->
            val serviceResult = serviceContainer.tryGetService(type)

            serviceResult.errorMessage?.let { errorMessage ->
                throwUser(
                    parameter.function,
                    "Parameter #${parameter.index} of type '${type.simpleName}' and name '${parameter.bestName}' does not have any compatible resolver and service loading failed: $errorMessage"
                )
            }

            ServiceCustomResolver(serviceResult.getOrThrow())
        }
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