package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.text.TextCommandsListener.Status
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging

@BService(priority = Int.MAX_VALUE - 1) // May be important logs
internal class TextCommandStatusLogger(config: BTextConfig, jdaService: JDAService, textPrefixSupplier: TextPrefixSupplier?) {
    init {
        fun prefixSupplierName() = textPrefixSupplier!!.javaClass.simpleNestedName

        val logger = KotlinLogging.loggerOf<BTextConfig>()
        when (Status.check(config, jdaService, textPrefixSupplier)) {
            Status.DISABLED -> {}
            Status.ENABLED -> {}
            Status.USES_PREFIX_SUPPLIER ->
                logger.info { "Listening to text commands, only using prefixes from ${prefixSupplierName()}" }
            Status.CAN_READ_PING ->
                logger.info { "Listening to text commands, only using ping-as-prefix" }
            Status.MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITH_PING ->
                logger.debug { "Text command prefixes supplied by ${prefixSupplierName()} are set but can't be used without GatewayIntent.MESSAGE_CONTENT, using ping-as-prefix" }
            Status.MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITHOUT_PING ->
                logger.info { "Disabling text commands as prefixes supplied by ${prefixSupplierName()} can't be used without GatewayIntent.MESSAGE_CONTENT, and ${BTextConfig::usePingAsPrefix.reference} is disabled" }
            Status.MISSING_CONTENT_INTENT_WITH_PREFIX_WITH_PING ->
                logger.debug { "Text command prefixes are set but can't be used without GatewayIntent.MESSAGE_CONTENT, using ping-as-prefix" }
            Status.MISSING_CONTENT_INTENT_WITH_PREFIX_WITHOUT_PING ->
                logger.info { "Disabling text commands as prefixes can't be used without GatewayIntent.MESSAGE_CONTENT, and ${BTextConfig::usePingAsPrefix.reference} is disabled" }
        }
    }
}