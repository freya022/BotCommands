package com.freya02.botcommands.api.core.service

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import kotlin.reflect.KClass

/**
 * Interface to supply services of the requested type.
 *
 * **Usage:** Register your instance as a service with [BService], and a [ServiceType] of [DynamicSupplier].
 *
 * @see InterfacedService
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

    fun getInstantiability(context: BContext, clazz: KClass<*>): Instantiability

    fun get(context: BContext, clazz: KClass<*>): Any
}