package com.freya02.botcommands.api.core.service.annotations

import com.freya02.botcommands.api.core.config.BConfigBuilder
import com.freya02.botcommands.api.core.service.DynamicSupplier
import com.freya02.botcommands.api.core.service.ServiceStart

/**
 * Marks this class as a service, or this function as a service factory.
 *
 * By default, the service is eagerly loaded at startup, when it is in the [framework's classpath][BConfigBuilder.addSearchPath].
 *
 * **Note:** The service will always( be loaded eagerly if it has an event listener, be it a command, autocomplete, a modal handler, etc...
 *
 * @see InjectedService
 * @see ConditionalService
 * @see ServiceType
 *
 * @see DynamicSupplier
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class BService( //Parameters tied to BServiceConfig#registerService
    /**
     * When the service should be started
     * @see ServiceStart
     */
    val start: ServiceStart = ServiceStart.DEFAULT,
    /**
     * The unique name of this service.
     *
     * The default is the name of the type the service is inserted as, but with the first letter lowercase.
     */
    val name: String = ""
)
