package io.github.freya022.botcommands.api.core.service.annotations

import org.springframework.context.annotation.Primary as SpringPrimary

/**
 * Defines a *primary* service provider.
 *
 * A primary service provider is prioritized over a normal service provider,
 * having multiple primary service providers is allowed, but the service's type might not be retrievable.
 *
 * See the "Primary providers" section in [@BService][BService].
 *
 * @see BService @BService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@SpringPrimary
annotation class Primary
