package io.github.freya022.botcommands.api.pagination

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.core.Logging.getLogger
import io.github.freya022.botcommands.api.core.utils.toEditData
import io.github.freya022.botcommands.api.pagination.paginator.BasicPaginatorBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture

private val LOGGER = getLogger()
private val TIMEOUT_SERVICE: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

/**
 * @param T Type of the implementor
 */
abstract class BasicPagination<T : BasicPagination<T>> protected constructor(
    protected val componentsService: Components,
    builder: BasicPaginatorBuilder<*, T>
) {
    protected val constraints: InteractionConstraints = builder.constraints
    protected val timeout: TimeoutInfo<T>? = builder.timeout

    private val usedIds: MutableSet<String> = hashSetOf()

    //TODO nullable
    private lateinit var timeoutFuture: ScheduledFuture<*>
    private var message: Message? = null

    private var timeoutPassed = false

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
     * Sets the [Message] associated to this paginator
     *
     * This is an optional operation and will only provide the [Message] object through the [PaginationTimeoutConsumer] you have set in your paginator builder
     *
     * **This message instance is not updated, this should only help you get the message's ID and not what's inside it**
     *
     * @param message The [Message] object associated to this paginator
     * @see BasicPaginationBuilder.setTimeout
     */
    //TODO move to property setter, enable getter
    fun setMessage(message: Message) {
        this.message = message
    }

    /**
     * Restarts the timeout of this pagination instance
     *
     * This means the timeout will be scheduled again, as if you called [.get], but without changing the actual content
     */
    open fun restartTimeout() {
        if (timeout != null) {
            check(!timeoutPassed) {
                "Timeout has already been cleaned up by pagination is still used ! Make sure you called BasicPagination#cleanup in the timeout consumer, timeout consumer at: ${timeout.onTimeout.javaClass.nestHost}"
            }

            if (::timeoutFuture.isInitialized)
                timeoutFuture.cancel(false)

            //Can't supply instance on by calling super constructor
            // Also don't want to do an abstract T getThis()
            timeoutFuture = TIMEOUT_SERVICE.schedule({
                timeoutPassed = true
                timeout.onTimeout.accept(this as T, message)
            }, timeout.timeout, timeout.unit)
        }
    }

    protected open fun preProcess(builder: MessageCreateBuilder) { }

    abstract fun writeMessage(builder: MessageCreateBuilder)

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
    fun cancelTimeout() {
        if (::timeoutFuture.isInitialized) {
            timeoutFuture.cancel(false)
        }
    }

    /**
     * Cleans up the button IDs used in this paginator
     *
     * This will remove every stored button IDs, even then buttons you included yourself
     */
    fun cleanup() {
        componentsService.deleteComponentsByIdJava(usedIds)

        usedIds.clear()
    }
}
