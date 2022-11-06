package com.freya02.botcommands.api.core.annotations

/**
 * Annotates a class as an injected service.
 *
 * The service needs to be instantiated and registered manually.
 *
 * @see BService
 * @see LazyService
 * @see ConditionalService
 * @see ServiceType
 */
@Target(AnnotationTarget.CLASS)
annotation class InjectedService
