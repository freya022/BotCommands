package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.InstanceSupplier
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.toImmutableMap
import kotlin.reflect.KClass

@InjectedService
interface BServiceConfig {
    val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>>
}

class BServiceConfigBuilder internal constructor() : BServiceConfig {
    private val _instanceSupplierMap: MutableMap<KClass<*>, InstanceSupplier<*>> = hashMapOf()
    override val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>>
        get() = _instanceSupplierMap.toImmutableMap()

    /**
     * Adds a supplier which returns instances of the specified classes
     *
     * This is used when creating services
     */
    fun <T : Any> registerInstanceSupplier(clazz: Class<T>, instanceSupplier: InstanceSupplier<T>) {
        _instanceSupplierMap[clazz.kotlin] = instanceSupplier
    }

    @JvmSynthetic
    internal fun build() = object : BServiceConfig {
        override val instanceSupplierMap = this@BServiceConfigBuilder.instanceSupplierMap
    }
}
