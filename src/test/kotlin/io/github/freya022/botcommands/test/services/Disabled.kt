package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition

@Condition(Disabled.DisabledChecker::class)
annotation class Disabled {
    object DisabledChecker : CustomConditionChecker<Disabled> {
        override val annotationType: Class<Disabled>
            get() = Disabled::class.java

        override fun checkServiceAvailability(
            serviceContainer: ServiceContainer,
            checkedClass: Class<*>,
            annotation: Disabled,
        ) = "Disabled"
    }
}