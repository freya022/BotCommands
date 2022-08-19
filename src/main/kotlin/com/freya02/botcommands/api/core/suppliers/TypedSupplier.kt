package com.freya02.botcommands.api.core.suppliers

import kotlin.reflect.KClass

interface TypedSupplier {
    val type: KClass<*>
}
