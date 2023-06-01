package com.freya02.botcommands.api.core.annotations

/**
 * Annotates a class as an injected service.
 *
 * The service needs to be instantiated and registered manually.
 *
 * This may be good for situations where services are defined by strategies (see Strategy design pattern),
 * an example would be other services could depend on the interface marked as an InjectedService.
 *
 * @see BService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class InjectedService(val message: String = "This service does not exist yet, it may be created under certain conditions")
