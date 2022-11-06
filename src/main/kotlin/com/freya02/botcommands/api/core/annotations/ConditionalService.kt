package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.ConditionalServiceChecker
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
     * Whether the service should be initialized lazily
     */
    val lazy: Boolean = false,
    /**
     * Makes this service depend on others, this also makes you able to skip the [ConditionalServiceChecker] implementation
     *
     * This may be useful in situations where the service shares some checks with another service
     */
    val dependencies: Array<KClass<*>> = [],
    /**
     * The message displayed if this class cannot be instantiated
     */
    @Deprecated("Replaced with the interface return value")
    val message: String = "Conditional object"
)
