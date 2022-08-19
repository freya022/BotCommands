package com.freya02.botcommands.api.core.annotations

/**
 * Annotation used to set a companion object function as the function which determines if a service is instantiable
 *
 * The function needs to:
 * - Be in a companion object
 * - Return a `String?`, which is the error message
 *
 * The function can use any parameters
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ConditionalServiceCheck
