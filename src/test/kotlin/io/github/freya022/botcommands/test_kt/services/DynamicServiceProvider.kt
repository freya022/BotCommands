package io.github.freya022.botcommands.test_kt.services

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.DynamicSupplier
import io.github.freya022.botcommands.api.core.service.DynamicSupplier.Instantiability
import io.github.freya022.botcommands.api.core.service.annotations.BService
import kotlin.reflect.KClass

@BService
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