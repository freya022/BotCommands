package io.github.freya022.botcommands.internal.core.service.stack

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.provider.Instance
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.TimedInstantiation
import io.github.freya022.botcommands.internal.core.service.stack.ServiceCreationStack.Companion.logger
import java.util.ArrayDeque
import java.util.Deque
import kotlin.time.DurationUnit

internal class ServiceCreationStackImpl : ServiceCreationStack {
    private val localSet: ThreadLocal<Deque<ServiceProvider>> = ThreadLocal.withInitial { ArrayDeque() }
    private val set get() = localSet.get()

    override fun contains(provider: ServiceProvider) = set.any { it.providerKey == provider.providerKey }

    //If services have circular dependencies during checking, consider it to not be an issue
    override fun withServiceCheckKey(provider: ServiceProvider, block: () -> ServiceError?): ServiceError? {
        if (contains(provider))
            return null
        set.addLast(provider)

        try {
            return block()
        } finally {
            set.removeLast()
        }
    }

    override fun <R : Instance> withServiceCreateKey(provider: ServiceProvider, block: () -> TimedInstantiation<R>): R {
        if (contains(provider))
            throw IllegalStateException("Circular dependency detected, list of the services being created : [${set.joinToString(" -> ")}] ; attempted to create ${provider.providerKey}")
        set.addLast(provider)

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
            set.removeLast()
        }
    }
}
