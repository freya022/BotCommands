package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.ServiceStart
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Indicates the class is a service that might be available under certain conditions.
 *
 * You will need to implement [ConditionalServiceChecker], or have dependencies.
 *
 * @see BService
 * @see InjectedService
 * @see ConditionalServiceChecker
 * @see ServiceType
 */
@Inherited
@Target(AnnotationTarget.CLASS)
annotation class ConditionalService(
    /**
     * When the service should be started
     * @see ServiceStart
     */
    val start: ServiceStart = ServiceStart.DEFAULT,
    /**
     * Makes this service depend on others, this also makes you able to skip the [ConditionalServiceChecker] implementation
     *
     * This may be useful in situations where the service shares some checks with another service
     */
    val dependencies: Array<KClass<*>> = []
)
