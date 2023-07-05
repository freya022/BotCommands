package com.freya02.botcommands.internal.core.waiter

import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.utils.logger
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.waiter.EventWaiter
import com.freya02.botcommands.api.waiter.EventWaiterBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExceptionHandler
import com.freya02.botcommands.internal.utils.EventUtils
import com.freya02.botcommands.internal.utils.Utils
import com.freya02.botcommands.internal.utils.throwInternal
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeoutException
import java.util.function.Predicate

private val logger = KotlinLogging.logger<EventWaiter>()

@BService
internal class EventWaiterImpl(context: BContextImpl) : EventWaiter {
    private val exceptionHandler = ExceptionHandler(context, logger)

    private val waitingMap: MutableMap<Class<out Event>, MutableList<WaitingEvent<out Event>>> = HashMap()
    private var commandThreadNumber = 0
    private val waiterCompleteService: ExecutorService = Utils.createCommandPool { r: Runnable ->
        Thread(r, "Event waiter thread #${commandThreadNumber++}").apply {
            isDaemon = false
            uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { t: Thread, e: Throwable ->
                Utils.printExceptionString("An unexpected exception happened in an event waiter thread '${t.name}':", e)
            }
        }
    }

    private lateinit var jda: JDA
    private lateinit var intents: EnumSet<GatewayIntent>

    override fun <T : Event> of(eventType: Class<T>): EventWaiterBuilder<T> {
        if (!::jda.isInitialized) {
            throw IllegalStateException("Cannot use the event waiter before a JDA instance has been detected")
        }

        EventUtils.checkEvent(jda, intents, eventType)

        return EventWaiterBuilderImpl(this, eventType)
    }

    internal fun <T : Event> submit(waitingEvent: WaitingEvent<T>): CompletableFuture<T> {
        val future = waitingEvent.completableFuture
        if (waitingEvent.timeout != null) {
            future.orTimeout(waitingEvent.timeout, waitingEvent.timeoutUnit)
        }

        val waitingEvents = waitingMap.computeIfAbsent(waitingEvent.eventType) { CopyOnWriteArrayList() }
        future.whenCompleteAsync({ t: T?, throwable: Throwable? ->
            try {
                waitingEvent.onComplete?.accept(future, t, throwable)
                if (throwable is TimeoutException) {
                    logger.trace { "Timeout for ${waitingEvent.eventType.simpleNestedName} waiter" }
                    //Not removed automatically by Iterator#remove before this method is called
                    waitingEvents.remove(waitingEvent)
                    waitingEvent.onTimeout?.run()
                } else if (t != null) {
                    waitingEvent.onSuccess?.accept(t)
                } else if (future.isCancelled) {
                    logger.trace { "Cancelled ${waitingEvent.eventType.simpleNestedName} waiter" }
                    //Not removed automatically by Iterator#remove before this method is called
                    waitingEvents.remove(waitingEvent)
                    waitingEvent.onCancelled?.run()
                } else {
                    throwInternal("Unexpected branch with stack trace: ${throwable?.stackTraceToString()}")
                }
            } catch (e: Exception) {
                exceptionHandler.handleException(t, e, "EventWaiter Future#whenCompleteAsync")
            }
        }, waiterCompleteService)

        waitingEvents.add(waitingEvent)

        return future
    }

    @Suppress("UNCHECKED_CAST")
    @BEventListener // Just listen to any event, I just need any JDA instance
    internal fun onEvent(event: Event, eventDispatcher: EventDispatcher) {
        if (!::jda.isInitialized) {
            logger.trace("Got JDA instance")

            this.jda = event.jda
            this.intents = event.jda.gatewayIntents
        }

        val waitingEvents: MutableList<WaitingEvent<out Event>> = waitingMap[event.javaClass] ?: return

        val iterator = waitingEvents.iterator()
        eventLoop@ while (iterator.hasNext()) {
            try {
                val waitingEvent = iterator.next()
                for ((index, precondition) in waitingEvent.preconditions.withIndex()) {
                    precondition as Predicate<Event>
                    if (!precondition.test(event)) {
                        logger.trace { "Failed ${event.javaClass.simpleNestedName} precondition #$index $precondition with $event" }
                        continue@eventLoop
                    }
                }

                val completableFuture = waitingEvent.completableFuture as CompletableFuture<Event>
                if (completableFuture.complete(event)) {
                    // Thread safe as underlying list is a CopyOnWriteArrayList,
                    // existing iterators won't be affected
                    iterator.remove()
                } else {
                    throwInternal("Completable future was already completed")
                }
            } catch (e: Exception) {
                exceptionHandler.handleException(event, e, "EventWaiter handler for $event")
            }
        }
    }
}
