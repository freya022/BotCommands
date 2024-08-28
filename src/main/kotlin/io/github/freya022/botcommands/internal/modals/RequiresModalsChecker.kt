package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.config.BModalsConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.internal.utils.reference

internal object RequiresModalsChecker : CustomConditionChecker<RequiresModals> {
    override val annotationType: Class<RequiresModals> = RequiresModals::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresModals
    ): String? {
        return if (serviceContainer.getService<BModalsConfig>().enable) {
            null
        } else {
            "Modals needs to be enabled, see ${BModalsConfig::enable.reference}"
        }
    }
}