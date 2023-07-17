package com.freya02.botcommands.internal.core.waiter

import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.InjectedJDAEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.api.core.utils.logger
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.core.waiter.EventWaiter
import com.freya02.botcommands.api.core.waiter.EventWaiterBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExceptionHandler
import com.freya02.botcommands.internal.utils.EventUtils
import com.freya02.botcommands.internal.utils.throwInternal
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Predicate
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger<EventWaiter>()

@BService
@ServiceType(EventWaiter::class)
internal class EventWaiterImpl(context: BContextImpl) : EventWaiter {
    private val exceptionHandler = ExceptionHandler(context, logger)

    private val waitingMap: MutableMap<Class<out Event>, MutableList<WaitingEvent<out Event>>> = HashMap()
    private val lock = ReentrantLock()

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

        val waitingEvents = waitingMap.computeIfAbsent(waitingEvent.eventType) { arrayListOf() }
        future.whenComplete { t: T?, throwable: Throwable? ->
            try {
                waitingEvent.onComplete?.accept(future, t, throwable)
                if (throwable is TimeoutException) {
                    logger.trace { "Timeout for ${waitingEvent.eventType.simpleNestedName} waiter" }
                    //Not removed automatically by Iterator#remove before this method is called
                    lock.withLock { waitingEvents.remove(waitingEvent) }
                    waitingEvent.onTimeout?.run()
                } else if (t != null) {
                    waitingEvent.onSuccess?.accept(t)
                } else if (future.isCancelled) {
                    logger.trace { "Cancelled ${waitingEvent.eventType.simpleNestedName} waiter" }
                    //Not removed automatically by Iterator#remove before this method is called
                    lock.withLock { waitingEvents.remove(waitingEvent) }
                    waitingEvent.onCancelled?.run()
                } else {
                    throwInternal("Unexpected branch with stack trace: ${throwable?.stackTraceToString()}")
                }
            } catch (e: Exception) {
                exceptionHandler.handleException(t, e, "EventWaiter Future#whenCompleteAsync")
            }
        }

        lock.withLock { waitingEvents.add(waitingEvent) }

        return future
    }

    @BEventListener
    internal fun onInjectedJDA(event: InjectedJDAEvent) {
        logger.trace { "Got JDA instance ${event.jda}" }

        this.jda = event.jda
        this.intents = event.jda.gatewayIntents
    }

    @Suppress("UNCHECKED_CAST")
    @BEventListener
    internal fun onEvent(event: Event, eventDispatcher: EventDispatcher) {
        val waitingEvents: MutableList<WaitingEvent<out Event>> = waitingMap[event.javaClass] ?: return

        lock.withLock {
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
}
