package com.freya02.botcommands.api.core.annotations

/**
 * Indicates the class is a service that will be created later
 *
 * Has no use other than static analysis
 */
@Target(AnnotationTarget.CLASS)
@Deprecated("Replaced with InjectedService")
annotation class LateService
