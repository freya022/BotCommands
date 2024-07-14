package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.utils.reference
import net.dv8tion.jda.api.requests.GatewayIntent

internal object RequiresTextCommandsChecker : CustomConditionChecker<RequiresTextCommands> {
    override val annotationType: Class<RequiresTextCommands> = RequiresTextCommands::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresTextCommands
    ): String? {
        val config = serviceContainer.getService<BTextConfig>()
        if (!config.enable) {
            return "Text commands needs to be enabled, see ${BTextConfig::enable.reference}"
        } else {
            val jdaService = serviceContainer.getService<JDAService>()
            if (GatewayIntent.MESSAGE_CONTENT !in jdaService.intents && !config.usePingAsPrefix) {
                return "GatewayIntent.MESSAGE_CONTENT is missing and ${BTextConfig::usePingAsPrefix.reference} is disabled"
            }
        }

        return null
    }
}