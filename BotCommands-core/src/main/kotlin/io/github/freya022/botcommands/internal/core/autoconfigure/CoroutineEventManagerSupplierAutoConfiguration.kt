package io.github.freya022.botcommands.internal.core.autoconfigure

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class CoroutineEventManagerSupplierAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(ICoroutineEventManagerSupplier::class)
    open fun iCoroutineEventManagerSupplier(): ICoroutineEventManagerSupplier {
        return ICoroutineEventManagerSupplier {
            CoroutineEventManager(namedDefaultScope("Bot coroutine", 4))
        }
    }
}
