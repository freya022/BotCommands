package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.config.BModalsConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.canCreateService
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals

internal object RequiresModalsChecker : CustomConditionChecker<RequiresModals> {
    override val annotationType: Class<RequiresModals> = RequiresModals::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresModals
    ): String? {
        if (serviceContainer.canCreateService<BModalsConfig>() == null) {
            return null
        }

        return "Modals needs to be registered or enabled"
    }
}
