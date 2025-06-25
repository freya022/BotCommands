package io.github.freya022.botcommands.internal.localization.autoconfigure

import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.localization.DefaultPermissionLocalization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class PermissionLocalizationAutoConfiguration internal constructor() {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(PermissionLocalization::class)
    open fun permissionLocalization(localizationService: LocalizationService): PermissionLocalization {
        return DefaultPermissionLocalization(localizationService)
    }
}
