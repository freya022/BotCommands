package com.freya02.botcommands.api.core.service.annotations

import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Marks a service as requiring other services.
 *
 * @see BService
 * @see ConditionalService
 * @see InjectedService
 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Dependencies(
    /**
     * An array of services required by this service.
     */
    val value: Array<KClass<*>>
)
