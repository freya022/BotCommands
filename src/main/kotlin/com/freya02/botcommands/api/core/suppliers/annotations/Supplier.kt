package com.freya02.botcommands.api.core.suppliers.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Supplier(val type: KClass<*>)
