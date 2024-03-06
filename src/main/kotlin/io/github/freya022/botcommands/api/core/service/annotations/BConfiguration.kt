package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder

/**
 * No-op annotation to mark a `class` / `object` as containing service factories.
 *
 * As a reminder, service factories must either be static, be in an `object`,
 * or be declared in a service class (in which case you need to use [@BService][BService] or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations] instead).
 *
 * @see BService @BService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class BConfiguration
