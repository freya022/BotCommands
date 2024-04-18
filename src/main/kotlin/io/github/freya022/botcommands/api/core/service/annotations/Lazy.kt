package io.github.freya022.botcommands.api.core.service.annotations

import org.springframework.context.annotation.Lazy as SpringLazy

/**
 * Defines a service provider as being lazy.
 *
 * A lazy service provider is only used when the service it provides gets requested.
 *
 * @see BService @BService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@SpringLazy
annotation class Lazy