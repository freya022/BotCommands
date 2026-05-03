package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.canCreateService

internal class RequiresComponentsChecker : CustomConditionChecker<RequiresComponents> {
    override val annotationType: Class<RequiresComponents> = RequiresComponents::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresComponents
    ): String? {
        if (serviceContainer.canCreateService<BComponentsConfig>() == null) {
            return null
        }

        return "The components module needs to be registered or enabled"
    }
}
