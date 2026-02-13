package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.annotations.RequiresLocalComponents
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.LocalComponentsConfig
import io.github.freya022.botcommands.api.core.config.getConfigOrNull
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.utils.classRef

internal object RequiresLocalComponentsChecker : CustomConditionChecker<RequiresLocalComponents> {
    override val annotationType: Class<RequiresLocalComponents> = RequiresLocalComponents::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresLocalComponents
    ): String? {
        return check(serviceContainer)
    }

    internal fun check(serviceContainer: ServiceContainer): String? {
        if (serviceContainer.getService<BConfig>().getConfigOrNull<LocalComponentsConfig>() == null) {
            return "Local components needs to be enabled, see ${classRef<LocalComponentsConfig>()}"
        }

        return null
    }
}
