package io.github.freya022.botcommands.internal.core.service

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * Makes a service disabled when using Spring
 */
@Conditional(DefaultInjectionCondition::class)
internal annotation class RequiresDefaultInjection

internal class DefaultInjectionCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return false
    }
}

