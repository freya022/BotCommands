package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.InstanceSupplier
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.core.service.DefaultServiceContainerImpl
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmName

internal class SuppliedServiceProvider internal constructor(
    private val clazz: KClass<*>,
    private val supplier: InstanceSupplier<*>
) : ServiceProvider {
    override var instance: Any? = null

    override val annotations = clazz.annotations
    override val name = getServiceName(clazz)
    override val providerKey = clazz.jvmName
    override val primaryType get() = clazz
    override val types = getServiceTypes(primaryType)
    override val isPrimary = hasAnnotation<Primary>()
    override val isLazy = hasAnnotation<Lazy>()
    override val priority = getAnnotatedServicePriority()

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
            supplier.supply(serviceContainer.getService())
        }
    }

    override fun getProviderFunction(): KFunction<*>? = null

    override fun getProviderSignature(): String = "<supplied ${clazz.shortQualifiedName}>"

    override fun toString(): String {
        return "SuppliedServiceProvider(supplier=$supplier, clazz=$clazz, instance=$instance)"
    }
}