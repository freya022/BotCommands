package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.ServiceStart
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.suppliers.annotations.DynamicSupplier
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier

/**
 * Marks this class as a service.
 *
 * By default, the service is eagerly loaded at startup, when it is in the [framework's classpath][BConfig.addSearchPath].
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
