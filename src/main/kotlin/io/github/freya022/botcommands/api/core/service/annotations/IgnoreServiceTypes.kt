package io.github.freya022.botcommands.api.core.service.annotations

import kotlin.reflect.KClass

/**
 * Removes the specified types a service would have been registered as.
 *
 * This may be useful if you extend/implement an interfaced service but don't want it to be retrieved as such.
 *
 * @see InterfacedService @InterfacedService
 * @see BService @BService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreServiceTypes(@get:JvmName("value") vararg val types: KClass<*>)
