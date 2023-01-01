package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.InstanceSupplier
import com.freya02.botcommands.api.core.annotations.InjectedService
import kotlin.reflect.KClass

@InjectedService
class BServiceConfig internal constructor() {
    @get:JvmSynthetic
    internal val instanceSupplierMap: MutableMap<KClass<*>, InstanceSupplier<*>> = hashMapOf()

    /**
     * Adds a supplier which returns instances of the specified classes
     *
     * This is used when creating services
     */
    fun <T : Any> registerInstanceSupplier(clazz: Class<T>, instanceSupplier: InstanceSupplier<T>) {
        instanceSupplierMap[clazz.kotlin] = instanceSupplier
    }
}
