package io.github.freya022.botcommands.core.hooks

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.hooks.EventListenerRegistry
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContains

object EventListenerRegistryTests {
    @Test
    fun `Listeners of Any are forbidden`() {
        class TestListener {
            fun onReady(@Suppress("unused") event: Any) {}
        }

        val ex = assertThrows<IllegalArgumentException> {
            EventListenerRegistry(
                BConfigBuilder().build(),
                mockk<ServiceContainer>(),
                mockk<JDAService>(),
                mockk<FunctionAnnotationsMap> {
                    every { get<BEventListener>() } returns listOf(
                        ClassPathFunction(TestListener(), TestListener::onReady),
                    )
                },
            )
        }

        assertContains(ex.message.orEmpty(), "Function must have a first parameter with a superclass of: [GenericEvent, BGenericEvent]")
    }
}
