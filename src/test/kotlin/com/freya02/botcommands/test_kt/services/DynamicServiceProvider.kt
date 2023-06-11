package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.DynamicSupplier
import com.freya02.botcommands.api.core.service.DynamicSupplier.Instantiability
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import kotlin.reflect.KClass

@BService
@ServiceType(DynamicSupplier::class)
object DynamicServiceProvider : DynamicSupplier {
    override fun getInstantiability(context: BContext, clazz: KClass<*>): Instantiability {
        if (clazz == Serv2::class) return Instantiability.instantiable()

        return Instantiability.unsupportedType()
    }

    override fun get(context: BContext, clazz: KClass<*>): Any {
        if (clazz == Serv2::class) {
            return Serv2()
        }

        throw AssertionError("Instantiability should have been checked first")
    }
}