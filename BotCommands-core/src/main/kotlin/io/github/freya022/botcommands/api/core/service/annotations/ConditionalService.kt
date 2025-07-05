package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.internal.core.service.annotations.HardcodedCondition
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Marks a service as being available under certain conditions.
 *
 * You are still required to mark this class as a service with [@BService][BService].
 *
 * All [ConditionalServiceChecker] must pass for this service to be instantiated.
 *
 * @see BService @BService
 * @see ConditionalServiceChecker
 */
@Inherited
@MustBeDocumented
@HardcodedCondition
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
annotation class ConditionalService( //TODO take this into account for configuration classes
    /**
     * Classes which implement [ConditionalServiceChecker], all checks must pass for this service to be instantiated.
     */
    @get:JvmName("value") vararg val checks: KClass<out ConditionalServiceChecker>
)
