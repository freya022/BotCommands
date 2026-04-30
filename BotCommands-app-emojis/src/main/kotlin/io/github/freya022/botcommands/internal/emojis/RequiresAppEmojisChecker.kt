package io.github.freya022.botcommands.internal.emojis

import io.github.freya022.botcommands.api.core.config.BAppEmojisConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.canCreateService
import io.github.freya022.botcommands.api.emojis.annotations.RequiresAppEmojis

internal class RequiresAppEmojisChecker : CustomConditionChecker<RequiresAppEmojis> {
    override val annotationType: Class<RequiresAppEmojis> = RequiresAppEmojis::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresAppEmojis
    ): String? {
        if (serviceContainer.canCreateService<BAppEmojisConfig>() == null) {
            return null
        }

        return "The app emojis module needs to be registered or enabled"
    }
}
