package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.utils.reference

internal object RequiresTextCommandsChecker : CustomConditionChecker<RequiresTextCommands> {
    override val annotationType: Class<RequiresTextCommands> = RequiresTextCommands::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresTextCommands
    ): String? {
        return if (serviceContainer.getService<BTextConfig>().enable) {
            null
        } else {
            "Text commands needs to be enabled, see ${BTextConfig::enable.reference}"
        }
    }
}