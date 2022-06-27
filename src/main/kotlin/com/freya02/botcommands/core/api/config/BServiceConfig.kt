package com.freya02.botcommands.core.api.config

import com.freya02.botcommands.api.ConstructorParameterSupplier
import com.freya02.botcommands.api.InstanceSupplier
import com.freya02.botcommands.core.api.suppliers.IDynamicInstanceSupplier
import com.freya02.botcommands.internal.MethodParameterSupplier
import java.util.function.Supplier
import kotlin.reflect.KClass

class BServiceConfig internal constructor() {
    val parameterSupplierMap: Map<KClass<*>, ConstructorParameterSupplier<*>> = HashMap()
    val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>> = HashMap()
    val dynamicInstanceSuppliers: List<IDynamicInstanceSupplier> = ArrayList()
    val commandDependencyMap: Map<KClass<*>, Supplier<*>> = HashMap()
    val methodParameterSupplierMap: Map<KClass<*>, MethodParameterSupplier<*>> = HashMap()
}
