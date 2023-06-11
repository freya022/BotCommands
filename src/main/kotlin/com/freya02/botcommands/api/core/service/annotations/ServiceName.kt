package com.freya02.botcommands.api.core.service.annotations

/**
 * Sets the name of this service.
 *
 * This annotation can be used for service declarations as well as when getting services
 *
 * @see BService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class ServiceName(
    /**
     * The unique name of this service.
     */
    val value: String
)
