package com.freya02.botcommands.api.core.service

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import com.freya02.botcommands.api.core.utils.ReflectionUtils
import kotlin.reflect.KClass

/**
 * Interface to supply services of the requested type.
 *
 * **Usage**: Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface DynamicSupplier {
    class Instantiability private constructor(internal val type: InstantiabilityType, val message: String?) {
        internal enum class InstantiabilityType {
            NOT_INSTANTIABLE,
            UNSUPPORTED_TYPE,
            INSTANTIABLE
        }

        companion object {
            fun notInstantiable(message: String) = Instantiability(InstantiabilityType.NOT_INSTANTIABLE, message)
            fun unsupportedType() = Instantiability(InstantiabilityType.UNSUPPORTED_TYPE, null)
            fun instantiable() = Instantiability(InstantiabilityType.INSTANTIABLE, null)
        }
    }

    /**
     * Returns the instantiability status of the requested service.
     *
     * @see Instantiability.InstantiabilityType
     * @see ReflectionUtils
     */
    fun getInstantiability(context: BContext, clazz: KClass<*>): Instantiability

    /**
     * Returns an instance of the requested service.
     *
     * This only runs if [getInstantiability] returned [Instantiability.InstantiabilityType.INSTANTIABLE]
     *
     * @see Instantiability.InstantiabilityType
     * @see ReflectionUtils
     */
    fun get(context: BContext, clazz: KClass<*>): Any
}