package io.github.freya022.botcommands.internal.components.serialization.autoconfigure

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.serialization.GlobalComponentDataSerializer
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@RequiresComponents
@InternalAutoConfiguration
internal open class GlobalComponentDataSerializerAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(GlobalComponentDataSerializer::class)
    open fun globalComponentDataSerializer(): GlobalComponentDataSerializer {
        return DefaultGlobalComponentDataSerializer
    }
}
