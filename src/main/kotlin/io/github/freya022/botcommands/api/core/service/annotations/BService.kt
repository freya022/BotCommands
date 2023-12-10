package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.commands.annotations.Optional
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.DynamicSupplier
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceStart

/**
 * Marks this class as a service, or this function as a service factory.
 *
 * Service factories must be static, or declared in a service class, or an `object`.
 *
 * **Warning:** Top-level functions are not processed, you must have them in an object/class.
 *
 * ### Service retrieval
 *
 * Classes annotated with [service annotations][BServiceConfigBuilder.serviceAnnotations]
 * can be injected into other service classes.
 * You can also request a named instance of a class by using [@ServiceName][ServiceName].
 *
 * #### Interfaced services
 *
 * Request a [List] will retrieve all services implementing the list's element type,
 * such as `List<ApplicationCommandFilter<*>>`.
 *
 * #### Lazy services
 *
 * In Kotlin, you can request a [ServiceContainer] and use a delegated property,
 * such as `private val helpCommand: IHelpCommand by serviceContainer.lazy()`.
 *
 * Java users can request a [Lazy] with the type being the requested service,
 * and get the value with [Lazy.getValue()][Lazy.value].
 *
 * **Note:** Lazy services cannot hold a list of interfaced services, nor can a list of lazy services be requested.
 *
 * #### Optional services
 *
 * If you want to get a service only if it is available, you can use Kotlin's [nullable](https://kotlinlang.org/docs/null-safety.html) / [optional](https://kotlinlang.org/docs/functions.html#default-arguments) parameters,
 * but Java users will need a [runtime-retained][AnnotationRetention.RUNTIME]
 * [@Nullable] annotation (such as [javax.annotation.Nullable], or, in checker-framework or JSpecify)
 * or [@Optional][Optional].
 *
 * Lazy services can also have their element type be marked as nullable, for example, `Lazy<@Nullable IHelpCommand>`.
 *
 * If a service is not available and the parameter is required, an exception will be thrown.
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
