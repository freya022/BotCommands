package com.freya02.botcommands.api.core.annotations

/**
 * Annotates a class as a service
 *
 * The service is loaded at startup and must be accessible in the classpath
 *
 * The service might not be loaded if [ConditionalService] is used
 */
@Target(AnnotationTarget.CLASS)
annotation class BService
