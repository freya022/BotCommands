package io.github.freya022.botcommands.api.pagination

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.core.Logging.getLogger
import io.github.freya022.botcommands.api.core.utils.toCreateData
import io.github.freya022.botcommands.api.pagination.paginator.BasicPaginatorBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
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

    protected val messageBuilder: MessageEditBuilder = MessageEditBuilder()
    protected val components: PaginatorComponents = PaginatorComponents()

    private val usedIds: MutableSet<String> = hashSetOf()

    //TODO nullable
    private lateinit var timeoutFuture: ScheduledFuture<*>
    private var message: Message? = null

    private var timeoutPassed = false

    /**
     * Returns the [MessageEditData] for this current page
     *
     * You can use this message edit data to edit a currently active pagination instance,
     * be aware that this will only replace the fields that already currently exist,
     * if you want to replace the whole message you need to call [MessageEditBuilder.setReplace] in your [paginator supplier][PaginatorSupplier]
     *
     * **You need to use [MessageCreateData.fromEditData] in order to send the initial message**
     *
     * @return The [MessageEditData] for this current page
     */
    //TODO this can probably be implemented here, only steps should be overridden
    abstract fun get(): MessageEditData

    fun getInitialMessage(): MessageCreateData = get().toCreateData()

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

    protected open fun onPreGet() {
        if (timeoutPassed && timeout != null) {
            //TODO throw
            LOGGER.warn(
                "Timeout has already been cleaned up by pagination is still used ! Make sure you called BasicPagination#cleanup in the timeout consumer, timeout consumer at: {}",
                timeout.onTimeout.javaClass.nestHost
            )
        }

        messageBuilder.clear()
        components.clear()

        restartTimeout()
    }

    /**
     * Restarts the timeout of this pagination instance
     *
     * This means the timeout will be scheduled again, as if you called [.get], but without changing the actual content
     */
    fun restartTimeout() {
        if (timeout != null) {
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

    protected open fun onPostGet() {
        //TODO take from message directly
        for (row in components.actionRows) {
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
