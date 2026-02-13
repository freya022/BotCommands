package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.config.LocalComponentsConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.reference

internal object RequiresComponentsChecker : CustomConditionChecker<RequiresComponents> {
    override val annotationType: Class<RequiresComponents> = RequiresComponents::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresComponents
    ): String? {
        val persistentError = RequiresPersistentComponentsChecker.check(serviceContainer)
        val localError = RequiresLocalComponentsChecker.check(serviceContainer)

        if (persistentError != null && localError != null) {
            return "Neither database-backed components or local components were enabled, see ${BComponentsConfig::enable.reference} or ${classRef<LocalComponentsConfig>()}"
        }

        // At least one works
        return null
    }
}
