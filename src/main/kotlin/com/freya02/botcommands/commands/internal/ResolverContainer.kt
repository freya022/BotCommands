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
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

@BService
internal class ResolverContainer(context: BContextImpl, classPathContainer: ClassPathContainer, serviceContainer: ServiceContainer) {
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
    fun onLoad(event: LoadEvent) {
        LOGGER.debug("ResolverContainer loaded")
        if (map.isEmpty()) {
            LOGGER.trace("Found no resolvers")
        } else {
            LOGGER.trace("Found resolvers:\n${map.entries.joinToString("\n") { it.key.java.simpleName }}")
        }
    }

    fun getResolver(type: KClass<*>) = map[type]
}