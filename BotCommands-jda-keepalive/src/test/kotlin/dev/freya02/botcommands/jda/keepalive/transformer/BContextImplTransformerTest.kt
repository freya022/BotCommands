package dev.freya02.botcommands.jda.keepalive.transformer

import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test

class BContextImplTransformerTest {

    @Test
    fun `BContextImpl is instrumented`() {
        assertDoesNotThrow {
            Class.forName("io.github.freya022.botcommands.internal.core.BContextImpl")
                .getDeclaredMethod("doScheduleShutdownSignal", Function0::class.java)
        }
    }
}
