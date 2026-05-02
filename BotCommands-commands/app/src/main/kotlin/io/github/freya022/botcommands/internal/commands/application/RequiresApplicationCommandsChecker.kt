package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.canCreateService

internal object RequiresApplicationCommandsChecker : CustomConditionChecker<RequiresApplicationCommands> {
    override val annotationType: Class<RequiresApplicationCommands> = RequiresApplicationCommands::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresApplicationCommands
    ): String? {
        if (serviceContainer.canCreateService<BApplicationConfig>() == null) {
            return null
        }

        return "The application commands module needs to be registered or enabled"
    }
}
