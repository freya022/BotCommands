package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.utils.reference

internal object RequiresApplicationCommandsChecker : CustomConditionChecker<RequiresApplicationCommands> {
    override val annotationType: Class<RequiresApplicationCommands> = RequiresApplicationCommands::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresApplicationCommands
    ): String? {
        if (serviceContainer.getService<BApplicationConfig>().enable) {
            return null
        }

        return "Application commands needs to be enabled, see ${BApplicationConfig::enable.reference}"
    }
}