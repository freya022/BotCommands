package io.github.freya022.botcommands.api.pagination

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.data.InteractionConstraints.Companion.empty
import io.github.freya022.botcommands.api.pagination.paginator.BasicPaginatorBuilder
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import net.dv8tion.jda.internal.utils.Checks
import java.util.concurrent.TimeUnit

/**
 * Most basic pagination builder.
 *
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 */
abstract class BasicPaginationBuilder<T : BasicPaginationBuilder<T, R>, R : BasicPagination<R>> protected constructor(
    protected val componentsService: Components
) {
    @Suppress("UNCHECKED_CAST")
    protected val instance: T get() = this as T

    var constraints: InteractionConstraints = empty()
        private set
    var timeout: TimeoutInfo<R>? = null
        private set

    @JvmSynthetic
    protected inline fun config(block: () -> Unit): T = instance.apply { block() }

    /**
     * Sets the timeout for this pagination instance
     *
     * **On timeout, only the consumer is called, no message are deleted, and it is up to you to clean up components with [BasicPagination.cleanup]**
     *
     * How to manipulate the message on timeout, for example, you want to delete the message, or replace its content:
     *
     * - For application commands: You can use the [Interaction hook][IDeferrableCallback.getHook] of application event
     * - For text commands: You can use [BasicPagination.setMessage] when the message has been sent successfully, so in your queue success consumer,
     * you will then receive that same message in the [PaginationTimeoutConsumer] you have set
     *
     *
     * @param timeout     Amount of time before the timeout occurs
     * @param timeoutUnit Unit of time for the supplied timeout
     * @param onTimeout   The consumer fired on timeout, long operations should not run here
     *
     * @return This builder for chaining convenience
     */
    fun setTimeout(timeout: Long, timeoutUnit: TimeUnit, onTimeout: PaginationTimeoutConsumer<R>): T = config {
        Checks.positive(timeout, "Timeout")

        this.timeout = TimeoutInfo(timeout, timeoutUnit, onTimeout)
    }

    //TODO timeout overloads, J/K duration

    /**
     * Sets the interaction constraints for this pagination object
     *
     * These constraints control who can use this pagination object, including [delete buttons][BasicPaginatorBuilder.useDeleteButton]
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
