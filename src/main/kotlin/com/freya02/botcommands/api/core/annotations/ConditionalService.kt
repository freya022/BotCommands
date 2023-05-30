package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.suppliers.annotations.DynamicSupplier
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier
import java.lang.annotation.Inherited

/**
 * Marks a service as being available under certain conditions.
 *
 * You are still required to mark this class as a service with [BService].
 *
 * You will need to implement [ConditionalServiceChecker], or have dependencies.
 *
 * @see BService
 * @see InjectedService
 * @see ConditionalServiceChecker
 * @see ServiceType
 *
 * @see InstanceSupplier
 * @see DynamicSupplier
 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class ConditionalService(
//    /**
//     * Makes this service depend on others, this also makes you able to skip the [ConditionalServiceChecker] implementation
//     *
//     * This may be useful in situations where the service shares some checks with another service
//     */
//    val dependencies: Array<KClass<*>> = []
)
