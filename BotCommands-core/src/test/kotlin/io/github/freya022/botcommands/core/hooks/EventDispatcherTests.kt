package io.github.freya022.botcommands.core.hooks

import dev.freya02.botcommands.method.accessors.internal.ClassFileMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.KotlinReflectMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.MethodAccessorFactory
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfigBuilder
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.hooks.EventDispatcherImpl
import io.github.freya022.botcommands.internal.core.hooks.EventHandlerFunction
import io.github.freya022.botcommands.internal.core.hooks.EventListenerList
import io.github.freya022.botcommands.internal.core.hooks.EventListenerRegistry
import io.github.freya022.botcommands.internal.core.method.accessors.MethodAccessorFactoryProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

object EventDispatcherTests {

    private class ExpectedException : Exception()

    @MethodSource("eventListenerAccessors")
    @ParameterizedTest
    fun `Exceptions in initialization events are propagated`(methodAccessorFactory: MethodAccessorFactory) {
        class ReadyTestListener {
            fun onReady(@Suppress("unused") event: BReadyEvent) {
                throw ExpectedException()
            }
        }

        // Test each accessor
        mockkObject(MethodAccessorFactoryProvider)
        every { MethodAccessorFactoryProvider.getAccessorFactory() } returns methodAccessorFactory

        val expectedInstance = ReadyTestListener()
        val expectedFunction = ReadyTestListener::onReady
        val listenerRegistry = mockk<EventListenerRegistry> {
            every { get(BReadyEvent::class.java) } returns EventListenerList().apply {
                add(
                    EventHandlerFunction(
                        BReadyEvent::class.java,
                        ClassPathFunction(expectedInstance, expectedFunction),
                        priority = 0,
                        runMode = BEventListener.RunMode.BLOCKING,
                        timeout = null,
                        parametersBlock = { emptyList() }
                    )
                )
            }
        }

        val dispatcher = EventDispatcherImpl(BCoroutineScopesConfigBuilder().build(), mockk<ServiceContainer> {
            every { getService<EventListenerRegistry>() } returns listenerRegistry
        })

        assertThrows<ExpectedException> { dispatcher.dispatchEventJava(mockk<BReadyEvent>()) }
    }

    @JvmStatic
    fun eventListenerAccessors(): List<Arguments> {
        return listOf(
            // To check if InvocationTargetException is unwrapped and checked
            Arguments.argumentSet("Reflection accessor", KotlinReflectMethodAccessorFactory()),
            // To check if direct exceptions are checked
            Arguments.argumentSet("Direct accessor", ClassFileMethodAccessorFactory()),
        )
    }
}
