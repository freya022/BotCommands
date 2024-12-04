package io.github.freya022.botcommands.internal.core.hooks

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.annotations.BEventListener.RunMode
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.events.InitializationEvent
import io.github.freya022.botcommands.api.core.hooks.EventDispatcher
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.unwrap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import net.dv8tion.jda.api.events.GenericEvent
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.callSuspend

private val logger = KotlinLogging.logger { }

internal class EventDispatcherImpl internal constructor(
    coroutineScopesConfig: BCoroutineScopesConfig,
    originalCoroutineEventManager: CoroutineEventManager,
    private val eventListenerRegistry: EventListenerRegistry,
) : EventDispatcher() {

    private val inheritedCoroutineScope: CoroutineScope = originalCoroutineEventManager
    private val asyncCoroutineScope: CoroutineScope = coroutineScopesConfig.eventDispatcherScope

    internal fun onEvent(event: GenericEvent) {
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = eventListenerRegistry[event::class] ?: return

        // Run blocking handlers first
        handlers[RunMode.BLOCKING]?.let { eventHandlers ->
            runBlocking {
                eventHandlers.forEach { eventHandler ->
                    runEventHandler(eventHandler, event)
                }
            }
        }

        // When the listener requests to run async
        handlers[RunMode.ASYNC]?.forEach { eventHandler ->
            asyncCoroutineScope.launch {
                runEventHandler(eventHandler, event)
            }
        }

        handlers[RunMode.INHERIT]?.let { eventHandlers ->
            // Stick to what JDA-KTX does, 1 coroutine per event for all listeners
            inheritedCoroutineScope.launch {
                eventHandlers.forEach { eventHandler ->
                    runEventHandler(eventHandler, event)
                }
            }
        }
    }

    override fun addEventListener(listener: Any): Unit = eventListenerRegistry.addEventListener(listener)

    override fun removeEventListener(listener: Any): Unit = eventListenerRegistry.removeEventListener(listener)

    override suspend fun dispatchEvent(event: Any) {
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = eventListenerRegistry[event::class] ?: return

        // Run blocking handlers first
        handlers[RunMode.BLOCKING]?.forEach { eventHandler ->
            runEventHandler(eventHandler, event)
        }

        // When the listener requests to run async
        handlers[RunMode.ASYNC]?.forEach { eventHandler ->
            asyncCoroutineScope.launch {
                runEventHandler(eventHandler, event)
            }
        }

        // Stick to what JDA-KTX does, 1 coroutine per event for all listeners
        handlers[RunMode.INHERIT]?.forEach { eventHandler ->
            runEventHandler(eventHandler, event)
        }
    }

    override fun dispatchEventAsync(event: Any): List<Deferred<Unit>> {
        // Try not to switch context on non-handled events
        // No need to check for `event` type as if it's in the map, then it's recognized
        val handlers = eventListenerRegistry[event::class] ?: return emptyList()

        return handlers.map { eventHandler ->
            asyncCoroutineScope.async { runEventHandler(eventHandler, event) }
        }
    }

    private suspend fun runEventHandler(eventHandlerFunction: EventHandlerFunction, event: Any) {
        try {
            val (instance, function) = eventHandlerFunction.classPathFunction

            /**
             * See [CoroutineEventManager.handle]
             */
            val actualTimeout = eventHandlerFunction.timeout
            if (actualTimeout.isPositive() && actualTimeout.isFinite()) {
                // Timeout only works when the continuations implement a cancellation handler
                val result = withTimeoutOrNull(actualTimeout.inWholeMilliseconds) {
                    function.callSuspend(instance, event, *eventHandlerFunction.parameters)
                }
                if (result == null) {
                    logger.debug { "Event of type ${event.javaClass.simpleName} timed out." }
                }
            } else {
                function.callSuspend(instance, event, *eventHandlerFunction.parameters)
            }
        } catch (e: InvocationTargetException) {
            if (event is InitializationEvent) {
                //Entry point will catch exception as it is the one dispatching the initialization events
                throw e.cause!!
            }

            if (e.cause is CancellationException) return

            printException(event, eventHandlerFunction, e)
        } catch (_: CancellationException) {
            // Ignore
        } catch (e: Throwable) {
            printException(event, eventHandlerFunction, e)
        }
    }

    private fun printException(event: Any, eventHandlerFunction: EventHandlerFunction, e: Throwable) =
        logger.error(e.unwrap()) {
            "An exception occurred while dispatching a ${event.javaClass.simpleNestedName} for ${eventHandlerFunction.classPathFunction.function.shortSignature}"
        }
}