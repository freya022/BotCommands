package io.github.freya022.botcommands.internal.emojis

import io.github.freya022.botcommands.api.core.config.BAppEmojisConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.emojis.annotations.RequiresAppEmojis
import io.github.freya022.botcommands.internal.utils.reference

internal class RequiresAppEmojisChecker : CustomConditionChecker<RequiresAppEmojis> {
    override val annotationType: Class<RequiresAppEmojis> = RequiresAppEmojis::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresAppEmojis
    ): String? {
        val config = serviceContainer.getService<BAppEmojisConfig>()
        if (!config.enable) {
            return "App emojis needs to be enabled, see ${BAppEmojisConfig::enable.reference}"
        }

        return null
    }
}