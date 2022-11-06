package com.freya02.botcommands.api.core.annotations

/**
 * Annotates a class as a service.
 *
 * The service is lazily loaded and must be in the classpath.
 *
 * @see BService
 * @see InjectedService
 * @see ConditionalService
 * @see ServiceType
 */
@Target(AnnotationTarget.CLASS)
annotation class LazyService
