package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceSupplier
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.core.service.DefaultServiceContainerImpl
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmName

internal class SuppliedServiceProvider internal constructor(
    serviceSupplier: ServiceSupplier<*>,
) : ServiceProvider {
    private var serviceSupplier: ServiceSupplier<*>? = serviceSupplier
    override var instance: Any? = null

    private val clazz = serviceSupplier.primaryType

    override val annotations = serviceSupplier.annotations
    override val name = serviceSupplier.name
    override val providerKey get() = clazz.jvmName
    override val primaryType get() = clazz
    override val types = serviceSupplier.additionalTypes + primaryType
    override val isPrimary = serviceSupplier.isPrimary
    override val isLazy = serviceSupplier.isLazy
    override val priority = serviceSupplier.priority

    override fun canInstantiate(serviceContainer: DefaultServiceContainerImpl): ServiceError? {
        return null
    }

    override fun createInstance(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation<*> {
        if (instance != null)
            throwInternal("Tried to create an instance of ${clazz.jvmName} when one already exists, instance should be retrieved manually beforehand")

        val timedInstantiation = createInstanceNonCached(serviceContainer)
        instance = timedInstantiation.instance
        return timedInstantiation
    }

    private fun createInstanceNonCached(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation<*> {
        return measureTimedInstantiation {
            val service = serviceSupplier!!.supplier(serviceContainer.getService())
            serviceSupplier = null // Let GC take what wont be used anymore
            service
        }
    }

    override fun getProviderFunction(): KFunction<*>? = null

    override fun getProviderSignature(): String = "<supplied ${clazz.shortQualifiedName}>"

    override fun toString(): String = providerKey
}