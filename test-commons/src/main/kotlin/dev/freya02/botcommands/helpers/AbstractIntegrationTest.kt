package dev.freya02.botcommands.helpers

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import org.junit.jupiter.api.AfterEach

abstract class AbstractIntegrationTest {
    protected lateinit var context: BContext
        private set

    @AfterEach
    fun cleanup() {
        if (::context.isInitialized) {
            context.shutdown()
        }
    }

    fun createTest(builder: BConfigBuilder.() -> Unit): BContext {
        check(!::context.isInitialized) {
            "Can't make more than two instances in one test"
        }

        context = BotCommands.create {
            disableExceptionsInDMs = true

            addClass<FakeBot>()

            builder()
        }

        return context
    }
}
