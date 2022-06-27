package com.freya02.botcommands.core.api.suppliers

import kotlin.reflect.KClass

interface TypedSupplier {
    val type: KClass<*>
}
