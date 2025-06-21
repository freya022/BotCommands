package io.github.freya022.botcommands.internal.core.replies

import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.replies.BuiltinRepliesFactory
import io.github.freya022.botcommands.api.core.replies.DefaultBuiltinRepliesFactory
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServiceTypes
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.localization.FallbackDefaultMessagesFactory
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

private val logger = KotlinLogging.logger { }

@AutoConfiguration
@ConditionalOnMissingBean(BuiltinRepliesFactory::class)
@BService
internal open class BuiltinRepliesFactoryProvider internal constructor() {

    @Bean
    @BService
    @ConditionalService(ActivationCondition::class)
    open fun builtinRepliesFactory(
        defaultMessagesFactory: DefaultMessagesFactory,
        localizationService: LocalizationService,
        textCommandLocaleProvider: TextCommandLocaleProvider,
        userLocaleProvider: UserLocaleProvider,
    ): BuiltinRepliesFactory {
        // Check if the user has a custom factory or if the fallback factory has customized files
        if (defaultMessagesFactory !is FallbackDefaultMessagesFactory || hasCustomDefaultMessages()) {
            logger.warn { "${classRef<DefaultMessagesFactory>()} has been deprecated and will be removed in the full release." }
            return BuiltinRepliesFactoryDefaultMessagesFactoryAdapter(defaultMessagesFactory)
        }

        return DefaultBuiltinRepliesFactory(localizationService, textCommandLocaleProvider, userLocaleProvider)
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
            val types = serviceContainer.getInterfacedServiceTypes<BuiltinRepliesFactory>()
            if (types.isNotEmpty()) {
                return "An user supplied ${classRef<BuiltinRepliesFactory>()} is already active (${types.first().simpleNestedName})"
            }

            return null
        }
    }
}