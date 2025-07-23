package io.github.freya022.botcommands.internal.core.hooks

import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.config.BEventManagerConfig
import io.github.freya022.botcommands.api.core.hooks.CoroutineEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.utils.takeIfFinite
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.unwrap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.IEventManager
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

private val logger = KotlinLogging.logger { }

/**
 * This class is more a barebone listener which replaces jda-ktx's CoroutineEventManager.
 *
 * Annotated listeners are still handled by [[EventDispatcherImpl]],
 * to which this listener delegates first in the caller thread (to support blocking listeners),
 * then runs regular [[EventListener]] subclasses in a new coroutine.
 */
@BService
@ServiceType(IEventManager::class)
internal class CoroutineEventManagerImpl internal constructor(
    private val eventDispatcher: EventDispatcherImpl,
    config: BEventManagerConfig,
    coroutineScopesConfig: BCoroutineScopesConfig,
) : IEventManager {

    private val defaultTimeout = config.defaultTimeout?.takeIfFinite()
    private val eventManagerCoroutineScope: CoroutineScope = coroutineScopesConfig.eventManagerScope

    private val listenerWrappersLock = ReentrantLock()
    private val listenerWrappers: MutableList<ListenerWrapper> = CopyOnWriteArrayList()

    override fun register(listener: Any): Unit = listenerWrappersLock.withLock {
        when (listener) {
            is CoroutineEventListener -> {
                val timeout = listener.timeout ?: defaultTimeout
                if (timeout != null) {
                    TimeConstrainedCoroutineListenerWrapper(listener, timeout)
                } else {
                    UnconstrainedCoroutineListenerWrapper(listener)
                }
            }
            is EventListener -> UnconstrainedListenerWrapper(listener)
            else -> throwArgument("Listener must implement either EventListener or CoroutineEventListener")
        }.also(listenerWrappers::add)
    }

    override fun unregister(listener: Any): Unit = listenerWrappersLock.withLock {
        val index = listenerWrappers.indexOfFirst { it.listener == listener }
        if (index < 0) return@withLock
        listenerWrappers.removeAt(index)
    }

    override fun handle(event: GenericEvent) {
        eventDispatcher.onEvent(event)

        // Run "regular" listeners
        eventManagerCoroutineScope.launch {
            listenerWrappers.forEach { wrapper ->
                try {
                    wrapper.runListener(event)
                } catch (_: CancellationException) {
                    // Ignore
                } catch (e: Throwable) {
                    logger.error(e.unwrap()) {
                        "An exception occurred in ${wrapper.listener} (${wrapper.listener.javaClass.simpleNestedName})"
                    }
                }
            }
        }
    }

    override fun getRegisteredListeners(): List<Any> {
        return listenerWrappers.map { it.listener }.unmodifiableView()
    }

    private sealed interface ListenerWrapper {
        val listener: Any

        suspend fun runListener(event: GenericEvent)
    }

    private class UnconstrainedListenerWrapper(
        override val listener: EventListener,
    ) : ListenerWrapper {

        override suspend fun runListener(event: GenericEvent) {
            listener.onEvent(event)
        }
    }

    private class UnconstrainedCoroutineListenerWrapper(
        override val listener: CoroutineEventListener,
    ) : ListenerWrapper {

        override suspend fun runListener(event: GenericEvent) {
            listener.onEvent(event)
        }
    }

    private class TimeConstrainedCoroutineListenerWrapper(
        override val listener: CoroutineEventListener,
        private val timeout: Duration,
    ) : ListenerWrapper {

        override suspend fun runListener(event: GenericEvent) {
            val result = withTimeoutOrNull(timeout.inWholeMilliseconds) {
                listener.onEvent(event)
            }

            if (result == null) {
                logger.debug { "Event listener $listener timed out on ${event.javaClass.name}" }
            }
        }
    }
}
