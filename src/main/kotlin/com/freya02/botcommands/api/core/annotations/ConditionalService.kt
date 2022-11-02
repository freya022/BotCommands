package com.freya02.botcommands.api.core.annotations

/**
 * Indicates the class is a service that might be available under certain conditions
 *
 * May include stuff like services related to components
 *
 * Has no use other than static analysis
 */
@Target(AnnotationTarget.CLASS)
annotation class ConditionalService(val message: String = "Conditional object") //Error message may be useful here in case the requested object is an interface
