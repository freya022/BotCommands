package io.github.freya022.botcommands.api.pagination

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.data.InteractionConstraints.Companion.empty
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.paginator.AbstractPaginatorBuilder
import kotlin.time.Duration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

/**
 * Most basic pagination builder.
 *
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 */
abstract class AbstractPaginationBuilder<T : AbstractPaginationBuilder<T, R>, R : AbstractPagination<R>> protected constructor(
    protected val context: BContext
) {
    @Suppress("UNCHECKED_CAST")
    protected val instance: T get() = this as T

    var constraints: InteractionConstraints = empty()
        private set
    var timeout: TimeoutInfo<R>? = TimeoutInfo(Components.defaultTimeout, onTimeout = null)
        private set

    @JvmSynthetic
    protected inline fun config(block: () -> Unit): T = instance.apply { block() }

    /**
     * Sets the timeout for this pagination instance.
     *
     * On timeout, only the consumer is called, no messages are deleted,
     * and it is up to you to clean up components with [AbstractPagination.cleanup].
     *
     * See [AbstractPagination.message] to get the message in the timeout consumer.
     *
     * The default timeout is set to [Components.defaultTimeout].
     *
     * @param timeout     Duration before the pagination expires
     * @param onTimeout   The consumer fired on timeout, long operations should not run here
     *
     * @return This builder for chaining convenience
     */
    fun setTimeout(timeout: JavaDuration, onTimeout: BlockingPaginationTimeoutConsumer<R>?): T =
        setTimeout(timeout.toKotlinDuration(), onTimeout?.let { { onTimeout.accept(it) } })

    /**
     * Sets the timeout for this pagination instance.
     *
     * On timeout, only the consumer is called, no messages are deleted,
     * and it is up to you to clean up components with [AbstractPagination.cleanup].
     *
     * See [AbstractPagination.message] to get the message in the timeout consumer.
     *
     * The default timeout is set to [Components.defaultTimeout].
     *
     * @param timeout     Duration before the pagination expires
     * @param onTimeout   The consumer fired on timeout, long operations should not run here
     *
     * @return This builder for chaining convenience
     */
    @JvmSynthetic
    fun setTimeout(timeout: Duration, onTimeout: SuspendingPaginationTimeoutConsumer<R>?): T = config {
        check(timeout.isFinite() && timeout.isPositive()) {
            "Timeout must be finite and positive"
        }

        this.timeout = TimeoutInfo(timeout, onTimeout)
    }

    /**
     * Disables the timeout for this pagination instance.
     *
     * @return This builder for chaining convenience
     */
    fun noTimeout(): T = config {
        this.timeout = null
    }

    /**
     * Sets the interaction constraints for this pagination object
     *
     * These constraints control who can use this pagination object, including [delete buttons][AbstractPaginatorBuilder.useDeleteButton]
     *
     * @param constraints The [InteractionConstraints] for this pagination object
     *
     * @return This builder for chaining convenience
     *
     * @see InteractionConstraints Factory methods of InteractionConstraints
     */
    fun setConstraints(constraints: InteractionConstraints): T = config {
        this.constraints = constraints
    }

    /**
     * Builds this pagination instance
     *
     * @return The newly created pagination instance
     */
    abstract fun build(): R
}
