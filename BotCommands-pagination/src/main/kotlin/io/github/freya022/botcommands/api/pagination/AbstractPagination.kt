package io.github.freya022.botcommands.api.pagination

import dev.freya02.botcommands.jda.ktx.messages.toEditData
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import kotlin.coroutines.cancellation.CancellationException

/**
 * @param T Type of the implementor
 */
abstract class AbstractPagination<T : AbstractPagination<T>> protected constructor(
    val context: BContext,
    builder: AbstractPaginationBuilder<*, T>
) {
    protected val logger by lazy { this@AbstractPagination.objectLogger() }

    protected val componentsService: Components get() = context.getService()
    protected val buttons: Buttons = context.getService()
    protected val selectMenus: SelectMenus get() = context.getService()
    protected val paginationTimeoutScope: CoroutineScope
        get() = context.coroutineScopesConfig.paginationTimeoutScope

    val constraints: InteractionConstraints = builder.constraints
    protected val timeout: TimeoutInfo<T>? = builder.timeout?.takeIf { it.timeout.isFinite() && it.timeout.isPositive() }

    private val usedComponents = UsedComponentSet(componentsService, builder.cleanAfterRefresh)

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
    fun getCurrentMessage(): MessageEditData = getInitialMessage().toEditData()

    /**
     * Returns the message data that represents the initial state of this pagination.
     *
     * @see getCurrentMessage
     */
    fun getInitialMessage(): MessageCreateData {
        restartTimeout()

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
                    "Cannot use this pagination instance after the timeout has elapsed"
                }

                timeoutJob.cancel()
            }

            timeoutJob = paginationTimeoutScope.launch {
                delay(timeout.timeout)
                onTimeout(timeout)
            }
        }
    }

    private suspend fun onTimeout(timeout: TimeoutInfo<T>) {
        try {
            timeoutPassed = true
            runCatching { cleanup() }.onFailure(::onTimeoutHandlerException)
            @Suppress("UNCHECKED_CAST")
            timeout.onTimeout?.invoke(this as T)
        } catch (e: Exception) {
            onTimeoutHandlerException(e)
        }
    }

    private fun onTimeoutHandlerException(e: Throwable) {
        if (e is CancellationException)
            return logger.trace(e) { "Pagination '${javaClass.shortQualifiedName}' timeout handler was cancelled" }

        ExceptionHandler(context, logger).handleException(
            event = null,
            e,
            locationDescription = "timeout handler",
            extraContext = mapOf(
                "Pagination type" to javaClass.name,
                "Timeout handler" to timeout?.onTimeout?.javaClass?.name,
            )
        )
    }

    protected open fun preProcess(builder: MessageCreateBuilder) { }

    protected abstract fun writeMessage(builder: MessageCreateBuilder)

    protected open fun postProcess(builder: MessageCreateBuilder) { }

    private fun saveUsedComponents(builder: MessageCreateBuilder) {
        usedComponents.setComponents(builder.componentTree)
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
    @JvmName("cleanup")
    fun cleanupJava() = runBlocking { cleanup() }

    /**
     * Cleans up the button IDs used in this paginator
     *
     * This will remove every stored button ID, even then buttons you included yourself
     */
    @JvmSynthetic
    suspend fun cleanup() {
        usedComponents.cleanup()
    }
}
