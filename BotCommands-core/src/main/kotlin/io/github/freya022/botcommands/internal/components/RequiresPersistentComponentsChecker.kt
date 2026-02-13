package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.components.annotations.RequiresPersistentComponents
import io.github.freya022.botcommands.internal.utils.reference

internal object RequiresPersistentComponentsChecker : CustomConditionChecker<RequiresPersistentComponents> {
    override val annotationType: Class<RequiresPersistentComponents> = RequiresPersistentComponents::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresPersistentComponents
    ): String? {
        return check(serviceContainer)
    }

    internal fun check(serviceContainer: ServiceContainer): String? {
        if (serviceContainer.getService<BComponentsConfig>().enable) {
            return null
        }

        return "Components needs to be enabled, see ${BComponentsConfig::enable.reference}"
    }
}
