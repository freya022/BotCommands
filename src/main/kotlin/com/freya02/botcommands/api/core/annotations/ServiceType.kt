package com.freya02.botcommands.api.core.annotations

import kotlin.reflect.KClass

/**
 * Changes the type this service is registered with
 *
 * This does **not** allow lazy loaded services to be loaded via their interfaces,
 * if you have a lazy loaded implementation, getting the interface as a service will not work until the implementation is loaded
 *
 * This may be useful in situations where impl files are services, but only the interface needs to be exposed
 *
 * @see BService
 * @see InjectedService
 * @see ConditionalService
 */
@Target(AnnotationTarget.CLASS)
annotation class ServiceType(
    /**
     * The type to register this service with
     */
    val type: KClass<*>,
    /**
     * Whether the created service's type should also be used along with the specified type
     */
    val keepOriginalType: Boolean = false
)
