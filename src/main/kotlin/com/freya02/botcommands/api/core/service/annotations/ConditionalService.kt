package com.freya02.botcommands.api.core.service.annotations

import com.freya02.botcommands.api.core.service.ConditionalServiceChecker
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
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ConditionalService(
    /**
     * Classes which implement [ConditionalServiceChecker], all checks must pass for this service to be instantiated.
     */
    vararg val checks: KClass<out ConditionalServiceChecker>
)
