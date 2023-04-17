package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.ServiceStart
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.suppliers.annotations.DynamicSupplier
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Marks this class as a service that might be available under certain conditions.
 *
 * By default, the service is eagerly loaded at startup, when it is in the [framework's classpath][BConfig.addSearchPath].
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
