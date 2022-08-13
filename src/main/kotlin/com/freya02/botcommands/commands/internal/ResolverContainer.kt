package com.freya02.botcommands.commands.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.core.internal.events.LoadEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.bestName
import com.freya02.botcommands.internal.rethrowUser
import com.freya02.botcommands.internal.runInitialization
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

@BService
internal class ResolverContainer( //TODO Should this be part of the base module ?
    context: BContextImpl,
    classPathContainer: ClassPathContainer,
    private val serviceContainer: ServiceContainer
) {
    private val map: MutableMap<KClass<*>, Any> = hashMapOf()

    init {
        classPathContainer
            .classes
            .filter { it.isSubclassOf(ParameterResolver::class) }
            .forEach { clazz -> addResolver(serviceContainer.getService(clazz) as ParameterResolver) }

        addResolver(CustomResolver(BContext::class.java) { _, _, _ -> context })
    }

    fun addResolver(resolver: ParameterResolver) {
        map[resolver.type.jvmErasure] = resolver
    }

    @BEventListener
    fun onLoad(event: LoadEvent) = runInitialization {
        LOGGER.debug("ResolverContainer loaded")
        if (map.isEmpty()) {
            LOGGER.trace("Found no resolvers")
        } else {
            LOGGER.trace("Found resolvers: [${map.entries.joinToString { it.key.java.simpleName }}]")
        }
    }

    fun getResolver(parameter: KParameter): Any {
        val type = parameter.type.jvmErasure

        return map[type] ?: run {
            val serviceResult = serviceContainer.tryGetService(type)

            serviceResult.onFailure {
                rethrowUser(
                    parameter.function,
                    "Parameter #${parameter.index} of type '${type.simpleName}' and name '${parameter.bestName}' does not have any compatible resolver and service loading failed",
                    it
                )
            }

            val service = serviceResult.getOrThrow()
            CustomResolver(service.javaClass) { _, _, _ -> service }
        }
    }
}