package io.github.freya022.botcommands.internal.core.service.annotations

import io.github.freya022.botcommands.internal.core.service.BCInjectionCondition
import org.springframework.context.annotation.Conditional

/**
 * Makes a service disabled when using Spring
 */
@Conditional(BCInjectionCondition::class)
internal annotation class RequiresDefaultInjection
