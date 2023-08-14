package com.freya02.botcommands.api.core.service.annotations

/**
 * Sets the priority of this service.
 *
 * Higher value = Will be loaded first/shown first in interfaces service lists
 *
 * @see BService @BService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ServicePriority(val value: Int)
