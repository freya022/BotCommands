package io.github.freya022.botcommands.api.core.service.annotations

/**
 * Sets the name of this service.
 *
 * This annotation can be used for service declarations as well as when getting services
 *
 * @see BService @BService
 * @see BService.name
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
annotation class ServiceName(
    /**
     * The unique name of this service.
     */
    val value: String
)
