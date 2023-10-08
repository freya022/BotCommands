package io.github.freya022.botcommands.api.core.service.annotations

import kotlin.reflect.KClass

/**
 * Adds additional types to register this service as.
 *
 * This may be useful in situations where implementation classes are services, but the interface needs to be constructible from it,
 * as the framework will find back the implementation from the declared service types
 *
 * @see BService @BService
 * @see InjectedService @InjectedService
 * @see ConditionalService @ConditionalService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ServiceType(
    /**
     * The additional types to register this service as, must be supertypes of this service
     */
    @get:JvmName("value") vararg val types: KClass<*>
)
