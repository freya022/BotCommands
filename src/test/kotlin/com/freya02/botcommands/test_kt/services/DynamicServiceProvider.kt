package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.core.suppliers.annotations.DynamicSupplier

@CommandMarker //Not needed, just for IJ
class DynamicServiceProvider {
    companion object {
        @DynamicSupplier
        fun supply(clazz: Class<*>): Any? {
            if (clazz == Serv2::class.java) {
                return Serv2()
            }

            return null
        }
    }
}