package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.DynamicSupplier
import io.github.freya022.botcommands.api.core.service.ServiceStart

/**
 * Marks this class as a service, or this function as a service factory.
 *
 * Service factories must be static, or declared in a service class, or an `object`.
 *
 * **Warning:** Top-level functions are not processed, you must have them in an object/class.
 *
 * ### Naming scheme
 *
 * By default, the services are named as such:
 * - For classes, the class's *nested* name but with the first letter lowercase.
 * (e.g., a `ChannelResolver` inside a `ChannelResolverFactory` => `channelResolverFactory.ChannelResolver`)
 * - For methods, the method's name.
 *
 * ### Loading order
 *
 * By default, the service is eagerly loaded at startup, when it is in the [framework's classpath][BConfigBuilder.addSearchPath].
 *
 * **Note:** The service will always be loaded eagerly if it has an event listener, be it a command, autocomplete, a modal handler, etc...
 *
 * **Note 2:** Service factories are prioritized over class annotations, see [ServicePriority] for more details.
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
     * By default, the services are named as such:
     * - For classes, the class's *nested* name but with the first letter lowercase.
     * (e.g., a `ChannelResolver` inside a `ChannelResolverFactory` => `channelResolverFactory.ChannelResolver`)
     * - For methods, the method's name.
     *
     * @see ServiceName @ServiceName
     */
    val name: String = "",
    /**
     * The priority of this service.
     *
     * Higher value = Will be loaded first/shown first in interfaced services lists.
     *
     * By default, service providers are sorted:
     * - By their priority
     * - If at the same priority, service factories are prioritized
     * - By their name (ascending alphabetical order)
     *
     * @see ServicePriority @ServicePriority
     */
    val priority: Int = 0
)
