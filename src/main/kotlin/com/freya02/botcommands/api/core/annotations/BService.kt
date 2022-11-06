package com.freya02.botcommands.api.core.annotations

/**
 * Annotates a class as a service.
 *
 * The service is eagerly loaded at startup and must be in the classpath.
 *
 * @see InjectedService
 * @see LazyService
 * @see ConditionalService
 * @see ServiceType
 */
@Target(AnnotationTarget.CLASS)
annotation class BService
