package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.commands.annotations.Optional
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.DynamicSupplier
import io.github.freya022.botcommands.api.core.service.LazyService
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import org.springframework.stereotype.Component

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
 *
 * In most cases, the services you retrieve will be type-matched, however, here is how they are looked up:
 * - If [@ServiceName][ServiceName] is used: Finds a service with the same name and satisfying type.
 * - If a parameter name is available (native in Kotlin, requires the `-parameters` compiler arg in Java):
 * Finds a service with the same name and a satisfying type.
 * - In other cases, a service with a satisfying type will be returned.
 *
 * #### Primary providers
 *
 * When requesting a service of a specific type, there must be at most one service provider for such a type.
 *
 * If multiple **usable** providers for the same type are present,
 * no service can be returned unless *one* [primary][Primary] provider is defined.
 *
 * For example, if you have two service factories with the same return type:
 * - ✗ If both are usable
 * - ✓ One has a failing condition, meaning you have one usable provider
 * - ✓ One is annotated with [@Primary][Primary], in which case this one is prioritized
 *
 * **Note:** You can still get all the [types][ServiceContainer.getInterfacedServiceTypes] / [instances][ServiceContainer.getInterfacedServices].
 *
 * #### Service types
 *
 * Registered services cannot be retrieved by every type they represent,
 * i.e., they can only be retrieved by the types they are explicitly registered as:
 * - Classes annotated as services are registered with the class type.
 * - Service factories (including property getters) register their returned services as the function's return type.
 *
 * In both cases, supertypes can be added using [@ServiceType][ServiceType]
 *
 * Using service factories, if your function creates an implementation class, but returns the abstract class/interface,
 * the service will only be retrievable by the latter.
 *
 * **Note:** Annotated functions in subclasses of services returned by service factories cannot be processed.
 * For example, if you have a [@BEventListener][BEventListener] in your implementation class,
 * but your service factory returns the abstract class/interface, then your listener will **not** work.
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
 * In both Kotlin and Java, you can request a [LazyService] with the requested service type
 * and get the value with [LazyService.getValue()][LazyService.value].
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
 * By default, the service is eagerly loaded at startup,
 * when it is in the [framework's classpath][BConfigBuilder.addSearchPath],
 * but you can make the service loaded only when requested, by using [@Lazy][Lazy].
 *
 * **Note:** The service can be loaded early if it has an event listener,
 * is a command, autocomplete, a modal handler, etc...
 *
 * **Note 2:** Service factories are prioritized over class annotations, see [ServicePriority] for more details.
 *
 * @see InjectedService @InjectedService
 * @see ConditionalService @ConditionalService
 * @see InterfacedService @InterfacedService
 *
 * @see Dependencies @Dependencies
 *
 * @see Lazy @Lazy
 * @see Primary @Primary
 * @see ServiceType @ServiceType
 * @see ServiceName @ServiceName
 * @see ServicePriority @ServicePriority
 *
 * @see DynamicSupplier
 */
@Component
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class BService(
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
