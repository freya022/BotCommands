package io.github.freya022.botcommands.core.hooks

import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.config.BEventManagerConfig
import io.github.freya022.botcommands.internal.core.hooks.CoroutineEventManagerImpl
import io.github.freya022.botcommands.internal.core.hooks.EventDispatcherImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration

class CoroutineEventManagerTest {

    private val config = mockk<BEventManagerConfig> {
        every { defaultTimeout } returns Duration.INFINITE
    }
    private val coroutineScopesConfig = mockk<BCoroutineScopesConfig>()

    @Test
    fun `JDA events are dispatched on the same thread`() {
        // The goal here is not to see if blocking listeners... block,
        // but rather check that [[CoroutineEventManagerImpl]]
        // doesn't make a new coroutine/thread before dispatching

        val expectedThread = Thread.currentThread()
        var wasSameThread = false
        val eventDispatcher = mockk<EventDispatcherImpl> {
            every { onEvent(any()) } answers {
                wasSameThread = expectedThread === Thread.currentThread()
            }
        }
        val event = mockk<GenericEvent>()
        withBlockingScope {
            val eventManager = CoroutineEventManagerImpl(eventDispatcher, config, coroutineScopesConfig)

            eventManager.handle(event)
        }

        verify(exactly = 1) { eventDispatcher.onEvent(event) }

        assertTrue(wasSameThread, "Thread should be the same")
    }

    @Test
    fun `JDA events are sent to the event dispatcher before regular listeners`() {
        var wasDispatched = false
        var listenerCalledAfterDispatch = false

        val listener = mockk<EventListener> {
            every { onEvent(any()) } answers {
                listenerCalledAfterDispatch = wasDispatched
            }
        }
        val event = mockk<GenericEvent>()

        withBlockingScope {
            val eventDispatcher = mockk<EventDispatcherImpl> {
                every { onEvent(any()) } answers {
                    wasDispatched = true
                }
            }
            val eventManager = CoroutineEventManagerImpl(eventDispatcher, config, coroutineScopesConfig)
            eventManager.register(listener)

            eventManager.handle(event)
        }

        assertTrue(listenerCalledAfterDispatch)

        verify(exactly = 1) { listener.onEvent(event) }
    }

    private fun withBlockingScope(block: () -> Unit) {
        runBlocking {
            every { coroutineScopesConfig.eventManagerScope } returns this@runBlocking

            block()
        }
    }
}
