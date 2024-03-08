package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.service.ServiceContainer

/**
 * No-op annotation marking a class as an injected service.
 *
 * The service needs to be instantiated and registered manually via [ServiceContainer.putService].
 *
 * @see BService @BService
 * @see MissingServiceMessage @MissingServiceMessage
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class InjectedService
