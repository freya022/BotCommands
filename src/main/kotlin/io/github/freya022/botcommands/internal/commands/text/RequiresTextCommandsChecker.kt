package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.text.TextCommandsListener.Status
import io.github.freya022.botcommands.internal.utils.reference

internal object RequiresTextCommandsChecker : CustomConditionChecker<RequiresTextCommands> {
    override val annotationType: Class<RequiresTextCommands> = RequiresTextCommands::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresTextCommands
    ): String? {
        fun prefixSupplierName() = serviceContainer.getService<TextPrefixSupplier>().javaClass.simpleNestedName
        return when (Status.check(serviceContainer.getService(), serviceContainer.getService(), serviceContainer.getServiceOrNull())) {
            Status.DISABLED -> "Text commands needs to be enabled, see ${BTextConfig::enable.reference}"
            Status.ENABLED -> null
            Status.USES_PREFIX_SUPPLIER -> null
            Status.CAN_READ_PING -> null
            Status.MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITH_PING -> null
            Status.MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITHOUT_PING -> "Text prefixes supplied by ${prefixSupplierName()} can't be used without GatewayIntent.MESSAGE_CONTENT, and ${BTextConfig::usePingAsPrefix.reference} is disabled"
            Status.MISSING_CONTENT_INTENT_WITH_PREFIX_WITH_PING -> null
            Status.MISSING_CONTENT_INTENT_WITH_PREFIX_WITHOUT_PING -> "Text prefixes can't be used without GatewayIntent.MESSAGE_CONTENT, and ${BTextConfig::usePingAsPrefix.reference} is disabled"
        }
    }
}