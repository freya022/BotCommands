package com.freya02.botcommands.api.core.service.annotations

/**
 * Sets the priority of this service.
 *
 * Higher value = Will be loaded first/shown first in interfaced services lists.
 *
 * By default, service providers are sorted:
 * - By their priority
 * - If at the same priority, service factories are prioritized
 * - By their name (ascending alphabetical order)
 *
 * @see BService @BService
 * @see BService.priority
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ServicePriority(val value: Int)
