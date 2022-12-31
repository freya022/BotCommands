package com.freya02.botcommands.api.core.suppliers.annotations

/**
 * TODO docs
 *
 * TODO convert to java annotation (for javadocs)
 *
 * If the function contains dependencies that cannot be supplied, then this supplier is skipped
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DynamicSupplier()
