package io.github.freya022.botcommands.internal.core.service.annotations

import io.github.freya022.botcommands.internal.core.service.DefaultInjectionCondition
import org.springframework.context.annotation.Conditional

/**
 * Makes a service disabled when using Spring
 */
@Conditional(DefaultInjectionCondition::class)
internal annotation class RequiresDefaultInjection