package com.freya02.botcommands.core.api.config

import com.freya02.botcommands.api.InstanceSupplier
import com.freya02.botcommands.core.api.annotations.LateService
import com.freya02.botcommands.core.api.suppliers.IDynamicInstanceSupplier
import kotlin.reflect.KClass

@LateService
class BServiceConfig internal constructor() {
    val instanceSupplierMap: MutableMap<KClass<*>, InstanceSupplier<*>> = hashMapOf()
    val dynamicInstanceSuppliers: MutableList<IDynamicInstanceSupplier> = arrayListOf()
}
