@file:Suppress("DEPRECATION")

package io.github.freya022.botcommands.internal.core.messages

import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.messages.DefaultBotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServiceTypes
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.localization.FallbackDefaultMessagesFactory
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

private val logger = KotlinLogging.logger { }

@BService
@AutoConfiguration
internal open class BotCommandsMessagesFactoryProvider internal constructor() {

    @Bean
    @ConditionalOnMissingBean(BotCommandsMessagesFactory::class)
    @BService
    @ConditionalService(ActivationCondition::class)
    open fun botCommandsMessagesFactory(
        defaultMessagesFactory: DefaultMessagesFactory,
        permissionLocalization: PermissionLocalization,
        localizationService: LocalizationService,
        textCommandLocaleProvider: TextCommandLocaleProvider,
        userLocaleProvider: UserLocaleProvider,
    ): BotCommandsMessagesFactory {
        // Check if the user has a custom factory or if the fallback factory has customized files
        if (defaultMessagesFactory !is FallbackDefaultMessagesFactory || hasCustomDefaultMessages()) {
            logger.warn { "${classRef<DefaultMessagesFactory>()} has been deprecated and will be removed in the full release." }
            return BotCommandsMessagesFactoryDefaultMessagesFactoryAdapter(defaultMessagesFactory)
        }

        return DefaultBotCommandsMessagesFactory(permissionLocalization, localizationService, textCommandLocaleProvider, userLocaleProvider)
    }

    private fun hasCustomDefaultMessages(): Boolean {
        // The base name is guaranteed to be "DefaultMessages" as it is hardcoded in [[DefaultMessages]]
        return ClassGraph()
            .acceptPathsNonRecursive("bc_localization")
            .scan()
            .use { scan ->
                scan.allResources
                    .any {
                        val path = it.path
                        path.startsWith("bc_localization/DefaultMessages") && !path.startsWith("bc_localization/DefaultMessages-default")
                    }
            }
    }

    internal object ActivationCondition : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            val types = serviceContainer.getInterfacedServiceTypes<BotCommandsMessagesFactory>()
            if (types.isNotEmpty()) {
                return "An user supplied ${classRef<BotCommandsMessagesFactory>()} is already active (${types.first().simpleNestedName})"
            }

            return null
        }
    }
}