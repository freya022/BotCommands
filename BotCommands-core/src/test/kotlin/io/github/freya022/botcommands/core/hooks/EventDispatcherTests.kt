package io.github.freya022.botcommands.core.hooks

import dev.freya02.botcommands.method.accessors.internal.KotlinReflectMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.MethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfigBuilder
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.hooks.EventDispatcherImpl
import io.github.freya022.botcommands.internal.core.hooks.EventHandlerFunction
import io.github.freya022.botcommands.internal.core.hooks.EventListenerList
import io.github.freya022.botcommands.internal.core.hooks.EventListenerRegistry
import io.github.freya022.botcommands.internal.core.method.accessors.MethodAccessorFactoryProvider
import io.mockk.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KFunction

object EventDispatcherTests {

    private class ExpectedException : Exception()

    @MethodSource("eventListenerAccessors")
    @ParameterizedTest
    fun `Exceptions in initialization events are propagated`(
        methodAccessorFactory: MethodAccessorFactory,
        expectedInstance: Any,
        expectedFunction: KFunction<*>,
        event: BReadyEvent,
    ) {
        // Test each accessor
        mockkObject(MethodAccessorFactoryProvider)
        every { MethodAccessorFactoryProvider.getAccessorFactory() } returns methodAccessorFactory

        val listenerRegistry = mockk<EventListenerRegistry> {
            every { get(BReadyEvent::class) } returns mockk<EventListenerList> {
                every { get(any<BEventListener.RunMode>()) } returns emptyList()
                every { get(BEventListener.RunMode.BLOCKING) } returns listOf(
                    EventHandlerFunction(
                        ClassPathFunction(expectedInstance, expectedFunction),
                        priority = 0,
                        runMode = BEventListener.RunMode.BLOCKING,
                        timeout = null,
                        parametersBlock = { emptyArray() }
                    )
                )
            }
        }

        val dispatcher = EventDispatcherImpl(BCoroutineScopesConfigBuilder().build(), listenerRegistry)

        assertThrows<ExpectedException> { dispatcher.dispatchEventJava(event) }
    }

    @JvmStatic
    fun eventListenerAccessors(): List<Arguments> {
        class ReadyTestListener {
            fun onReady(@Suppress("unused") event: BReadyEvent) {
                throw ExpectedException()
            }
        }

        val expectedInstance = ReadyTestListener()
        val expectedFunction = ReadyTestListener::onReady
        val event = mockk<BReadyEvent>()
        val directAccessor = mockk<MethodAccessorFactory> {
            every { create(expectedInstance, expectedFunction) } returns mockk<MethodAccessor<Unit>> {
                every { createBlankArguments() } returns mockk<MethodArguments> {
                    every { set(0, event) } just runs
                    every { get(0) } returns event
                    every { clone() } returns this
                }
                coEvery { callSuspend(any()) } answers { expectedInstance.onReady(event) }
            }
        }

        return listOf(
            // To check if InvocationTargetException is unwrapped and checked
            Arguments.argumentSet("Reflection accessor", KotlinReflectMethodAccessorFactory(), expectedInstance, expectedFunction, event),
            // To check if direct exceptions are checked
            Arguments.argumentSet("Direct accessor", directAccessor, expectedInstance, expectedFunction, event),
        )
    }
}
