package com.freya02.botcommands.api.commands.ratelimit.annotations

import com.freya02.botcommands.api.commands.ratelimit.RateLimitContainer
import com.freya02.botcommands.api.core.service.annotations.BService

/**
 * Annotation for functions which declares rate limits.
 *
 * The first argument must be a [RateLimitContainer].
 *
 * **Requirement:** The declaring class must be [a service][BService].
 *
 * @see BService @BService
 * @see RateLimitContainer.rateLimit
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimitDeclaration