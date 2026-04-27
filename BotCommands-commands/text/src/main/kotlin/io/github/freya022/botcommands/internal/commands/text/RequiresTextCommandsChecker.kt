package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.canCreateService

internal object RequiresTextCommandsChecker : CustomConditionChecker<RequiresTextCommands> {
    override val annotationType: Class<RequiresTextCommands> = RequiresTextCommands::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresTextCommands
    ): String? {
        if (serviceContainer.canCreateService<BTextConfig>() == null) {
            return null
        }

        return "The text commands module needs to be registered or enabled"
    }
}
