package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Marks a service as being available under certain conditions.
 *
 * You are still required to mark this class as a service with [BService].
 *
 * All [ConditionalServiceChecker] must pass for this service to be instantiated.
 *
 * @see BService @BService
 * @see ConditionalServiceChecker
 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ConditionalService(
    /**
     * Classes which implement [ConditionalServiceChecker], all checks must pass for this service to be instantiated.
     */
    @get:JvmName("value") vararg val checks: KClass<out ConditionalServiceChecker>
)
