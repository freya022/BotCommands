package com.freya02.botcommands.api.core.service.annotations

import com.freya02.botcommands.api.core.config.BConfigBuilder
import com.freya02.botcommands.api.core.service.DynamicSupplier
import com.freya02.botcommands.api.core.service.ServiceStart

/**
 * Marks this class as a service, or this function as a service factory.
 *
 * Service factories must be declared in a service class, or an `object`.
 *
 * By default, the service is eagerly loaded at startup, when it is in the [framework's classpath][BConfigBuilder.addSearchPath].
 *
 * **Note:** The service will always be loaded eagerly if it has an event listener, be it a command, autocomplete, a modal handler, etc...
 *
 * **Warning:** Top-level functions are not processed, you must have them in an object/class.
 *
 * @see InjectedService @InjectedService
 * @see ConditionalService @ConditionalService
 * @see InterfacedService @InterfacedService
 *
 * @see Dependencies @Dependencies
 *
 * @see ServiceType @ServiceType
 * @see ServiceName @ServiceName
 * @see ServicePriority @ServicePriority
 *
 * @see DynamicSupplier
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class BService( //Parameters tied to BServiceConfig#registerService
    /**
     * When the service should be started
     *
     * @see ServiceStart
     */
    val start: ServiceStart = ServiceStart.DEFAULT,
    /**
     * The unique name of this service.
     *
     * The default is the name of the type the service is inserted as, but with the first letter lowercase.
     *
     * @see ServiceName @ServiceName
     */
    val name: String = "",
    /**
     * The priority of this service.
     *
     * Higher value = Will be loaded first/shown first in interfaces service lists
     *
     * @see ServicePriority @ServicePriority
     */
    val priority: Int = 0
)
