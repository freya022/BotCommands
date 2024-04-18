package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.internal.core.service.annotations.HardcodedCondition
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Marks a service as requiring other services.
 *
 * Services that miss dependencies will not be instantiated and won't throw an exception.
 *
 * @see BService @BService
 * @see ConditionalService @ConditionalService
 * @see Condition @Condition
 * @see InjectedService @InjectedService
 */
@Inherited
@MustBeDocumented
@HardcodedCondition
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class Dependencies(
    /**
     * An array of services required by this service.
     */
    vararg val value: KClass<*>
)
