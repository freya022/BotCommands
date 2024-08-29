package io.github.freya022.botcommands.internal.core.service.stack

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.provider.Instance
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.TimedInstantiation
import io.github.freya022.botcommands.internal.core.service.stack.ServiceCreationStack.Companion.logger
import java.util.*
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

internal class TracedServiceCreationStack : ServiceCreationStack {
    private sealed class ServiceOperation<in V>(protected val provider: ServiceProvider) {
        private val mark = TimeSource.Monotonic.markNow()

        val providerKey get() = provider.providerKey
        val children: MutableList<ServiceOperation<*>> = arrayListOf()

        protected var elapsed: Duration by Delegates.notNull()
            private set

        fun setElapsedNow() {
            elapsed = mark.elapsedNow()
        }

        abstract fun onValue(value: V)

        context(StringBuilder)
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

        context(StringBuilder)
        override fun print(indent: Int) {
            append("  ".repeat(indent))

            val opDuration = elapsed.toString(DurationUnit.MILLISECONDS, decimals = 3)
            val typeName = provider.primaryType.simpleNestedName
            val failIndicator = if (hasFailed) " failed" else ""
            val errorMessage = if (error != null) " {${error?.toSimpleString()}}" else ""
            appendLine("[Check$failIndicator, $opDuration] $typeName$errorMessage ($providerKey)")

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

        context(StringBuilder)
        override fun print(indent: Int) {
            append("  ".repeat(indent))

            val opDuration = elapsed.toString(DurationUnit.MILLISECONDS, decimals = 3)
            if (::instance.isInitialized) {
                val typeName = instance::class.simpleNestedName
                val loadedAsTypes = provider.types.joinToString(prefix = "[", postfix = "]") { it.simpleNestedName }
                appendLine("[Create, $opDuration] $typeName as $loadedAsTypes ($providerKey)")
            } else {
                val typeName = provider.primaryType.simpleNestedName
                appendLine("[Create failed, $opDuration] $typeName ($providerKey)")
            }

            children.forEach { it.print(indent + 1) }
        }
    }

    private val localSet: ThreadLocal<LinkedList<ServiceOperation<*>>> = ThreadLocal.withInitial { LinkedList() }
    private val set: LinkedList<ServiceOperation<*>> get() = localSet.get()

    override fun contains(provider: ServiceProvider) = set.any { it.providerKey == provider.providerKey }

    //If services have circular dependencies during checking, consider it to not be an issue
    override fun withServiceCheckKey(provider: ServiceProvider, block: () -> ServiceError?): ServiceError? {
        return withServiceKey(provider, ::ServiceCheckOperation, block, onDuplicate = {
            return null
        })
    }

    override fun <R : Instance> withServiceCreateKey(
        provider: ServiceProvider,
        block: () -> TimedInstantiation<R>
    ): R {
        return withServiceKey(provider, ::ServiceCreateOperation, block, onDuplicate = {
            throw IllegalStateException("Circular dependency detected, list of the services being created : [${set.joinToString(" -> ")}] ; attempted to create ${provider.providerKey}")
        }).instance
    }

    private inline fun <R> withServiceKey(provider: ServiceProvider, operationSupplier: (ServiceProvider) -> ServiceOperation<R>, crossinline block: () -> R, onDuplicate: () -> Nothing): R {
        if (provider in this)
            onDuplicate() // Does not return

        val serviceOperation = operationSupplier(provider)

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
                logger.trace {
                    buildString { serviceOperation.print() }.trim()
                }
            }
        }
    }
}