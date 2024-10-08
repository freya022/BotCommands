package io.github.freya022.botcommands.api.core.service.annotations

import java.lang.annotation.Inherited
import kotlin.reflect.KClass
import org.springframework.context.annotation.Primary as SpringPrimary

/**
 * Removes the specified types a service would have been registered as.
 *
 * This may be useful if you extend/implement an interfaced service but don't want it to be retrieved as such.
 *
 * **Spring note:** This annotation has no effect when using Spring,
 * you will need to use [@Primary][SpringPrimary] on the service you'll want to inject by default.
 *
 * @see InterfacedService @InterfacedService
 * @see BService @BService
 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreServiceTypes(@get:JvmName("value") vararg val types: KClass<*>)
