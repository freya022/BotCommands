package io.github.freya022.botcommands.internal.core.service.stack

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.provider.Instance
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.TimedInstantiation
import io.github.freya022.botcommands.internal.core.service.stack.ServiceCreationStack.Companion.logger
import java.util.ArrayDeque
import java.util.Deque
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

internal class TracedServiceCreationStack : ServiceCreationStack {
    private sealed class ServiceOperation<in V>(val provider: ServiceProvider) {
        private val mark = TimeSource.Monotonic.markNow()

        val providerKey get() = provider.providerKey
        val children: MutableList<ServiceOperation<*>> = arrayListOf()

        protected var elapsed: Duration by Delegates.notNull()
            private set

        fun setElapsedNow() {
            elapsed = mark.elapsedNow()
        }

        abstract fun onValue(value: V)

        context(_: StringBuilder)
        abstract fun print(indent: Int = 0)

        // For circular dependency string
        override fun toString(): String {
            return providerKey
        }
    }

    private class ServiceCheckOperation(provider: ServiceProvider) : ServiceOperation<ServiceError?>(provider) {
        private var _error: Any? = NO_VALUE
        private val error: ServiceError? get() = _error as? ServiceError?
        private val hasFailed: Boolean get() = _error === NO_VALUE

        override fun onValue(value: ServiceError?) {
            _error = value
        }

        context(builder: StringBuilder)
        override fun print(indent: Int) {
            builder.append("  ".repeat(indent))

            val opDuration = elapsed.toString(DurationUnit.MILLISECONDS, decimals = 3)
            val typeName = provider.primaryType.simpleNestedName
            val failIndicator = if (hasFailed) " failed" else ""
            val errorMessage = if (error != null) " {${error?.toSimpleString()}}" else ""
            builder.appendLine("[Check$failIndicator, $opDuration] $typeName$errorMessage ($providerKey)")

            children.forEach { it.print(indent + 1) }
        }

        companion object {
            private val NO_VALUE = Any()
        }
    }

    private class ServiceCreateOperation(provider: ServiceProvider) : ServiceOperation<TimedInstantiation<*>>(provider) {
        // Null if the service creation fails
        private lateinit var instance: Instance

        override fun onValue(value: TimedInstantiation<*>) {
            instance = value.instance
        }

        context(builder: StringBuilder)
        override fun print(indent: Int) {
            builder.append("  ".repeat(indent))

            val opDuration = elapsed.toString(DurationUnit.MILLISECONDS, decimals = 3)
            if (::instance.isInitialized) {
                val typeName = instance::class.simpleNestedName
                val loadedAsTypes = provider.types.joinToString(prefix = "[", postfix = "]") { it.simpleNestedName }
                builder.appendLine("[Create, $opDuration] $typeName as $loadedAsTypes ($providerKey)")
            } else {
                val typeName = provider.primaryType.simpleNestedName
                builder.appendLine("[Create failed, $opDuration] $typeName ($providerKey)")
            }

            children.forEach { it.print(indent + 1) }
        }
    }

    private val localSet: ThreadLocal<Deque<ServiceOperation<*>>> = ThreadLocal.withInitial { ArrayDeque() }
    private val set: Deque<ServiceOperation<*>> get() = localSet.get()

    override val currentProviders: List<ServiceProvider>
        get() = set.map { it.provider }

    override fun contains(provider: ServiceProvider) = set.any { it.providerKey == provider.providerKey }

    //If services have circular dependencies during checking, consider it to not be an issue
    override fun withServiceCheckKey(provider: ServiceProvider, block: () -> ServiceError?): ServiceError? {
        if (contains(provider))
            return null

        val serviceOperation = ServiceCheckOperation(provider)
        return withServiceOperation(serviceOperation, block)
    }

    override fun <R : Instance> withServiceCreateKey(provider: ServiceProvider, block: () -> TimedInstantiation<R>): R {
        if (contains(provider))
            throw IllegalStateException("Circular dependency detected, list of the services being created : [${set.joinToString(" -> ")}] ; attempted to create ${provider.providerKey}")

        val serviceOperation = ServiceCreateOperation(provider)
        return withServiceOperation(serviceOperation, block).instance
    }

    private inline fun <R> withServiceOperation(serviceOperation: ServiceOperation<R>, crossinline block: () -> R): R {
        // Add the new OP to the children of the current OP
        if (set.isNotEmpty())
            set.last().children += serviceOperation

        set.addLast(serviceOperation)
        try {
            val value = block()
            serviceOperation.onValue(value)
            return value
        } finally {
            serviceOperation.setElapsedNow()
            set.removeLast()

            if (set.isEmpty()) {
                logOperations(serviceOperation)
            }
        }
    }

    private fun <R> logOperations(serviceOperation: ServiceOperation<R>) {
        logger.trace {
            buildString { serviceOperation.print() }.trim()
        }
    }
}