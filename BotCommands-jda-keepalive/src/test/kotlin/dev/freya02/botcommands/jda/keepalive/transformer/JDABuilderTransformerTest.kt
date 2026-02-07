package dev.freya02.botcommands.jda.keepalive.transformer

import dev.freya02.botcommands.jda.keepalive.internal.BufferingEventManager
import dev.freya02.botcommands.jda.keepalive.internal.JDABuilderConfiguration
import dev.freya02.botcommands.jda.keepalive.internal.JDABuilderSession
import io.mockk.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import okhttp3.OkHttpClient
import org.junit.jupiter.api.assertThrows
import java.util.function.Supplier
import kotlin.test.Test

class JDABuilderTransformerTest {

    @Test
    fun `Constructor is instrumented`() {
        val builderConfiguration = mockk<JDABuilderConfiguration>(relaxUnitFun = true)

        mockkObject(JDABuilderSession)
        every { JDABuilderSession.currentSession().configuration } answers { builderConfiguration }

        JDABuilder.create("MY_TOKEN", setOf(GatewayIntent.GUILD_MEMBERS))

        verify(exactly = 1) { builderConfiguration.onInit("MY_TOKEN", GatewayIntent.getRaw(GatewayIntent.GUILD_MEMBERS)) }
    }

    @Test
    fun `Unsupported instance method invalidates cache`() {
        // Initial set up, this *may* call "markIncompatible" so we need to do it before really mocking
        val builder = createJDABuilder()

        // Actual test
        val builderConfiguration = mockk<JDABuilderConfiguration> {
            every { onInit(any(), any()) } just runs
            every { markUnsupportedValue(any()) } just runs
        }

        mockkObject(JDABuilderSession)
        every { JDABuilderSession.currentSession().configuration } returns builderConfiguration

        builder.setHttpClientBuilder(OkHttpClient.Builder())

        verify(exactly = 1) { builderConfiguration.markUnsupportedValue(any()) }
    }

    @Test
    fun `Instance method is instrumented`() {
        // Initial set up, this *may* call "markIncompatible" so we need to do it before really mocking
        val builder = createJDABuilder()

        // Actual test
        val builderConfiguration = mockk<JDABuilderConfiguration> {
            every { onInit(any(), any()) } just runs
            every { setStatus(any()) } just runs
        }

        mockkObject(JDABuilderSession)
        every { JDABuilderSession.currentSession().configuration } returns builderConfiguration

        builder.setStatus(OnlineStatus.DO_NOT_DISTURB)

        verify(exactly = 1) { builderConfiguration.setStatus(OnlineStatus.DO_NOT_DISTURB) }
    }

    @Test
    fun `Build method is instrumented`() {
        val builderSession = mockk<JDABuilderSession> {
            every { onBuild(any()) } returns mockk()
            every { configuration } returns mockk<JDABuilderConfiguration>(relaxUnitFun = true)
        }

        mockkObject(JDABuilderSession)
        every { JDABuilderSession.currentSession() } returns builderSession

        JDABuilder.createDefault("MY_TOKEN").build()

        verify(exactly = 1) { builderSession.onBuild(any()) }
    }

    @Test
    fun `Build sets our event manager`() {
        val builderConfiguration = mockk<JDABuilderConfiguration>(relaxUnitFun = true)

        val builderSession = mockk<JDABuilderSession> {
            every { onBuild(any()) } answers { arg<Supplier<JDA>>(0).get() }
            every { configuration } returns builderConfiguration
        }

        mockkObject(JDABuilderSession)
        every { JDABuilderSession.currentSession() } returns builderSession

        val builder = spyk(JDABuilder.createDefault("MY_TOKEN").setEventManager(DummyEventManager))

        // The special event manager is set on JDABuilder#build() before any original code is run
        // so we'll throw an exception on the first method call of the original code,
        // which is checkIntents()
        every { builder["checkIntents"]() } throws ExpectedException()
        assertThrows<ExpectedException> { builder.build() }

        verify(exactly = 1) { builder.setEventManager(ofType<BufferingEventManager>()) }
    }

    /**
     * Creates a basic JDABuilder,
     * call this on the first line to not record any mocking data before doing the actual test.
     */
    private fun createJDABuilder(): JDABuilder {
        lateinit var builder: JDABuilder
        mockkObject(JDABuilderSession) {
            every { JDABuilderSession.currentSession().configuration } returns mockk(relaxUnitFun = true)

            builder = JDABuilder.create("MY_TOKEN", emptySet())
        }

        return builder
    }

    private object DummyEventManager : IEventManager {

        override fun register(listener: Any) {}

        override fun unregister(listener: Any) {}

        override fun handle(event: GenericEvent) {}

        override fun getRegisteredListeners(): List<Any?> = emptyList()
    }

    private class ExpectedException : RuntimeException()
}
