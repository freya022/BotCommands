package com.freya02.botcommands.api.core.suppliers.annotations

import kotlin.reflect.KClass

/**
 * TODO
 *
 * If the function contains dependencies that cannot be supplied, then this supplier is skipped
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Supplier(val type: KClass<*>)
