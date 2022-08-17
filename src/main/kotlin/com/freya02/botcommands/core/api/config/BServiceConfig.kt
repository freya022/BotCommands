package com.freya02.botcommands.core.api.config

import com.freya02.botcommands.api.InstanceSupplier
import com.freya02.botcommands.core.api.annotations.LateService
import com.freya02.botcommands.core.api.suppliers.IDynamicInstanceSupplier
import com.freya02.botcommands.core.api.suppliers.annotations.Supplier
import kotlin.reflect.KClass

@LateService
class BServiceConfig internal constructor() {
    @get:JvmSynthetic
    internal val instanceSupplierMap: MutableMap<KClass<*>, InstanceSupplier<*>> = hashMapOf()

    /**
     * Instance suppliers which are used to create instances of any type.
     *
     * Each instance supplier is run via its [@Supplier][Supplier] method, until one returns an object
     */
    internal val dynamicInstanceSuppliers: MutableList<IDynamicInstanceSupplier> = arrayListOf()

    /**
     * Adds a supplier which returns instances of the specified classes
     *
     * This is used when creating services
     */
    fun <T : Any> registerInstanceSupplier(clazz: Class<T>, instanceSupplier: InstanceSupplier<T>) {
        instanceSupplierMap[clazz.kotlin] = instanceSupplier
    }
}
