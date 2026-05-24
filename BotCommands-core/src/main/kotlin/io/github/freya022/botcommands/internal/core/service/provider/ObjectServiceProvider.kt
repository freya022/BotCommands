package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.core.service.BCServiceContainerImpl
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

internal class ObjectServiceProvider internal constructor(
    clazz: KClass<*>
) : AbstractClassServiceProvider(clazz) {
    override var instance: Any? = null

    override fun canInstantiate(serviceContainer: BCServiceContainerImpl): ServiceError? {
        // Objects can't have conditions
        return null
    }

    override fun createInstance(serviceContainer: BCServiceContainerImpl): TimedInstantiation<*> {
        val timedInstantiation = createInstanceNonCached()
        instance = timedInstantiation.instance
        return timedInstantiation
    }

    private fun createInstanceNonCached(): TimedInstantiation<*> {
        return TimedInstantiation.of { clazz.objectInstance!! }
    }

    override fun getProviderFunction(): KFunction<*>? {
        return null
    }

    override fun getProviderSignature(): String {
        return "<object ${clazz.shortQualifiedName}>"
    }
}
