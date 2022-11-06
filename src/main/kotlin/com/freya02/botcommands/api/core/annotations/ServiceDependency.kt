package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.ConditionalServiceChecker
import kotlin.reflect.KClass

/**
 * Annotates this class as being dependent on other services
 *
 * This annotation also allow you to not implement [ConditionalServiceChecker]
 *
 * This may be useful in situations where the service shares some checks with another service
 *
 * @see ConditionalService
 * @see ConditionalServiceChecker
 */
@Target(AnnotationTarget.CLASS)
annotation class ServiceDependency(val dependencies: Array<KClass<*>>)
