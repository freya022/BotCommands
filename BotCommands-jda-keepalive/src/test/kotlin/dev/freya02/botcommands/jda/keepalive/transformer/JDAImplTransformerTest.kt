package dev.freya02.botcommands.jda.keepalive.transformer

import dev.freya02.botcommands.jda.keepalive.internal.JDABuilderSession
import io.mockk.*
import net.dv8tion.jda.internal.JDAImpl
import kotlin.test.Test

class JDAImplTransformerTest {

    @Test
    fun `Shutdown method is instrumented`() {
        val builderSession = mockk<JDABuilderSession> {
            every { onShutdown(any(), any()) } just runs
        }

        val jda = mockk<JDAImpl> {
            // If this getter is missing, then the codegen changed
            every { this@mockk["getBuilderSession"]() } returns builderSession
            every { shutdown() } answers { callOriginal() }
        }

        jda.shutdown()

        verify(exactly = 1) { builderSession.onShutdown(jda, any()) }
    }
}
