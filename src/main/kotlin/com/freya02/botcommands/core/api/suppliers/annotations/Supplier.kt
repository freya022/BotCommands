package com.freya02.botcommands.core.api.suppliers.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Supplier(val type: KClass<*>)
