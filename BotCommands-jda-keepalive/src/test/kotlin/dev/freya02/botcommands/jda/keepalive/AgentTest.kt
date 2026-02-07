package dev.freya02.botcommands.jda.keepalive

import dev.freya02.botcommands.jda.keepalive.internal.Agent
import dev.freya02.botcommands.jda.keepalive.internal.exceptions.AlreadyLoadedClassesException
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.assertNotNull
import kotlin.test.Test

class AgentTest {

    @Test
    fun `Dynamically loaded agent throws when classes are already loaded`() {
        // Do a normal run to load classes
        Agent.transformers.keys.forEach {
            Class.forName("${it.packageName()}.${it.displayName()}")
        }

        // Capture AlreadyLoadedClassesException
        var agentmainException: AlreadyLoadedClassesException? = null
        mockkObject(Agent)
        every { Agent.checkNoLoadedClassesAreToBeTransformed(any()) } answers {
            try {
                callOriginal()
            } catch (e: AlreadyLoadedClassesException) {
                // This is thrown on a separate thread, so we need to capture it this way
                agentmainException = e
                // Don't throw
            }
        }

        // Load agent
        Agent.load()

        assertNotNull(agentmainException)
    }
}
