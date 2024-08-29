package io.github.freya022.botcommands.internal.core.service.stack

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.provider.Instance
import io.github.freya022.botcommands.internal.core.service.provider.ProviderName
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.TimedInstantiation
import io.github.freya022.botcommands.internal.core.service.stack.ServiceCreationStack.Companion.logger
import kotlin.time.DurationUnit

internal class DefaultServiceCreationStack : ServiceCreationStack {
    private val localSet: ThreadLocal<MutableSet<ProviderName>> = ThreadLocal.withInitial { linkedSetOf() }
    private val set get() = localSet.get()

    override fun contains(provider: ServiceProvider) = set.contains(provider.providerKey)

    //If services have circular dependencies during checking, consider it to not be an issue
    override fun withServiceCheckKey(provider: ServiceProvider, block: () -> ServiceError?): ServiceError? {
        if (!set.add(provider.providerKey)) return null
        try {
            return block()
        } finally {
            set.remove(provider.providerKey)
        }
    }

    override fun <R : Instance> withServiceCreateKey(provider: ServiceProvider, block: () -> TimedInstantiation<R>): R {
        check(set.add(provider.providerKey)) {
            "Circular dependency detected, list of the services being created : [${set.joinToString(" -> ")}] ; attempted to create ${provider.providerKey}"
        }
        try {
            val (instance, duration) = block()
            logger.trace {
                val instanceTypeName = instance.javaClass.simpleNestedName
                val loadedAsTypes = provider.types.joinToString(prefix = "[", postfix = "]") { it.simpleNestedName }
                val durationStr = duration.toString(DurationUnit.MILLISECONDS, decimals = 3)
                "Loaded service $instanceTypeName as $loadedAsTypes in $durationStr"
            }
            return instance
        } finally {
            set.remove(provider.providerKey)
        }
    }
}