package io.github.freya022.botcommands.internal.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.api.core.utils.simpleNestedName

@BService
@ConditionalService(DefaultCoroutineEventManagerSupplier.ExistingSupplierChecker::class)
internal class DefaultCoroutineEventManagerSupplier : ICoroutineEventManagerSupplier {
    override fun get(): CoroutineEventManager {
        return CoroutineEventManager(namedDefaultScope("Bot coroutine", 4))
    }

    internal object ExistingSupplierChecker : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            // Try to get CoroutineEventManagerSupplier interfaced services, except ours
            // If empty, then the user didn't provide one, in which case we can allow
            //Won't take DefaultCoroutineEventManagerSupplier into account
            val suppliers = serviceContainer.getInterfacedServices<ICoroutineEventManagerSupplier>()
            if (suppliers.isNotEmpty())
                return "An user supplied CoroutineEventManagerSupplier is already active (${suppliers.first().javaClass.simpleNestedName})"

            return null
        }
    }
}