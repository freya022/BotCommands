package io.github.freya022.botcommands.api.core.service.annotations

import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Adds additional types to register this service as.
 *
 * This may be useful in situations where implementation classes are services, but the interface needs to be constructible from it,
 * as the framework will find back the implementation from the declared service types.
 *
 * See the "Service types" section in [@BService][BService] for more details.
 *
 * @see BService @BService
 * @see InjectedService @InjectedService
 * @see ConditionalService @ConditionalService
 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
annotation class ServiceType(
    /**
     * The additional types to register this service as, must be supertypes of this service
     */
    @get:JvmName("value") vararg val types: KClass<*>
)
