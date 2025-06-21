package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServiceTypes
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.DefaultPermissionLocalization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.internal.utils.classRef
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnMissingBean(PermissionLocalization::class)
@BService
internal open class PermissionLocalizationProvider internal constructor() {

    @Bean
    @BService
    @ConditionalService(ActivationCondition::class)
    open fun permissionLocalization(localizationService: LocalizationService): PermissionLocalization {
        return DefaultPermissionLocalization(localizationService)
    }

    internal object ActivationCondition : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            val types = serviceContainer.getInterfacedServiceTypes<PermissionLocalization>()
            if (types.isNotEmpty()) {
                return "An user supplied ${classRef<PermissionLocalization>()} is already active (${types.first().simpleNestedName})"
            }

            return null
        }
    }
}