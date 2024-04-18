package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.utils.reference

internal class RequiresComponentsChecker : CustomConditionChecker<RequiresComponents> {
    override val annotationType: Class<RequiresComponents> = RequiresComponents::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresComponents
    ): String? {
        if (serviceContainer.getService<BComponentsConfig>().useComponents) {
            return null
        }

        return "Components needs to be enabled, see ${BComponentsConfig::useComponents.reference}"
    }
}