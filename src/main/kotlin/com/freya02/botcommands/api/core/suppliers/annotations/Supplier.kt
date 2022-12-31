package com.freya02.botcommands.api.core.suppliers.annotations

/**
 * TODO
 *
 * If the function contains dependencies that cannot be supplied, then this supplier is skipped
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Supplier()
