package io.github.freya022.botcommands.internal.commands.text.autoconfigure

import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService
import io.github.freya022.botcommands.internal.utils.reference

@InternalAutoConfiguration
internal open class HelpCommandAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(IHelpCommand::class)
    @RequiresTextCommands
    @ConditionalService(IsHelpDisabledChecker::class)
    open fun iHelpCommand(
        config: BTextConfig,
        messagesFactory: TextCommandsMessagesFactory,
        textCommandsContext: TextCommandsContext,
        helpBuilderConsumer: HelpBuilderConsumer?,
    ): IHelpCommand {
        return DefaultHelpCommand(config, messagesFactory, textCommandsContext, helpBuilderConsumer)
    }

    internal object IsHelpDisabledChecker : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            if (serviceContainer.getService<BTextConfig>().isHelpDisabled) {
                return "The help command was disabled in ${BTextConfig::isHelpDisabled.reference}"
            }

            return null
        }
    }
}
