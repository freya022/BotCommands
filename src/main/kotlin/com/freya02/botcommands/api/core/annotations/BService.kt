package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.ServiceStart
import com.freya02.botcommands.api.core.suppliers.annotations.DynamicSupplier
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier

/**
 * Annotates a class as a service.
 *
 * The service is eagerly loaded at startup and must be in the classpath.
 *
 * @see InjectedService
 * @see ConditionalService
 * @see ServiceType
 *
 * @see InstanceSupplier
 * @see DynamicSupplier
 */
@Target(AnnotationTarget.CLASS)
annotation class BService(
    /**
     * When the service should be started
     * @see ServiceStart
     */
    val start: ServiceStart = ServiceStart.DEFAULT
)
