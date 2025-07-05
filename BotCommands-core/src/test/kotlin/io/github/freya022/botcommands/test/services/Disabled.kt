package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata

@Condition(Disabled.DisabledChecker::class)
@Conditional(Disabled.DisabledChecker::class)
annotation class Disabled {
    object DisabledChecker : CustomConditionChecker<Disabled>, org.springframework.context.annotation.Condition {
        override val annotationType: Class<Disabled>
            get() = Disabled::class.java

        override fun checkServiceAvailability(
            serviceContainer: ServiceContainer,
            checkedClass: Class<*>,
            annotation: Disabled,
        ) = "Disabled"

        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean = false
    }
}