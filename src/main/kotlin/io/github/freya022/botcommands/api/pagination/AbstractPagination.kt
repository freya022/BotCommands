package io.github.freya022.botcommands.api.pagination

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.toEditData
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.utils.launchCatchingDelayed
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData

/**
 * @param T Type of the implementor
 */
abstract class AbstractPagination<T : AbstractPagination<T>> protected constructor(
    val context: BContext,
    builder: AbstractPaginationBuilder<*, T>
) {
    protected val componentsService: Components = context.getService()
    protected val paginationTimeoutScope: CoroutineScope
        get() = context.coroutineScopesConfig.paginationTimeoutScope

    protected val constraints: InteractionConstraints = builder.constraints
    protected val timeout: TimeoutInfo<T>? = builder.timeout.takeIf { it.timeout.isFinite() && it.timeout.isPositive() }

    private val usedIds: MutableSet<String> = hashSetOf()

    private lateinit var timeoutJob: Job
    private var timeoutPassed = false

    /**
     * The [Message] associated to this paginator
     *
     * You can optionally set this after sending the message in a channel.
     *
     * For interactions, you should rather use your [InteractionHook].
     */
    var message: Message? = null

    /**
     * Returns the message data that represents the current state of this pagination.
     *
     * You can use this message edit data to edit a currently active pagination instance.
     *
     * @see getInitialMessage
     */
    fun getCurrentMessage(): MessageEditData {
        restartTimeout()

        return getInitialMessage().toEditData()
    }

    /**
     * Returns the message data that represents the initial state of this pagination.
     *
     * @see getCurrentMessage
     */
    fun getInitialMessage(): MessageCreateData {
        val builder = MessageCreateBuilder()

        preProcess(builder)
        writeMessage(builder)
        postProcess(builder)
        saveUsedComponents(builder)

        return builder.build()
    }

    /**
     * Restarts the timeout of this pagination instance
     *
     * This means the timeout will be scheduled again, as if you called [.get], but without changing the actual content
     */
    open fun restartTimeout() {
        if (timeout != null) {
            if (::timeoutJob.isInitialized) {
                // The job cannot be rescheduled if the timeout handler has run
                check(!timeoutPassed) {
                    "Timeout has already been cleaned up by pagination is still used ! Make sure you called BasicPagination#cleanup in the timeout consumer, timeout consumer at: ${timeout.onTimeout?.javaClass?.nestHost}"
                }

                timeoutJob.cancel()
            }

            timeoutJob = paginationTimeoutScope.launchCatchingDelayed(timeout.timeout, { onTimeoutHandlerException(it) }) {
                timeoutPassed = true
                @Suppress("UNCHECKED_CAST")
                timeout.onTimeout?.invoke(this as T)
            }
        }
    }

    private fun onTimeoutHandlerException(e: Throwable) {
        ExceptionHandler(context, KotlinLogging.logger { }).handleException(null, e, "timeout handler", emptyMap())
    }

    protected open fun preProcess(builder: MessageCreateBuilder) { }

    protected abstract fun writeMessage(builder: MessageCreateBuilder)

    protected open fun postProcess(builder: MessageCreateBuilder) { }

    protected open fun saveUsedComponents(builder: MessageCreateBuilder) {
        for (row in builder.components) {
            for (component in row.actionComponents) {
                val id = component.id ?: continue

                usedIds.add(id)
            }
        }
    }

    /**
     * Cancels the timeout action for this pagination instance
     *
     * The timeout will be enabled back if the page changes
     */
    open fun cancelTimeout() {
        if (::timeoutJob.isInitialized) {
            timeoutJob.cancel()
        }
    }

    /**
     * Cleans up the button IDs used in this paginator
     *
     * This will remove every stored button ID, even then buttons you included yourself
     */
    fun cleanup() {
        componentsService.deleteComponentsByIdJava(usedIds)

        usedIds.clear()
    }
}
