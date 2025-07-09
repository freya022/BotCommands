package dev.freya02.botcommands.typesafe.messages.integration

import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(classes = [SpringIntegrationTest.Application::class])
class SpringIntegrationTest {

    @SpringBootApplication(scanBasePackages = ["dev.freya02.botcommands.typesafe.messages.integration"])
    open class Application

    @Autowired
    private lateinit var messageSourceFactory: MyMessageSourceFactory

    @Test
    fun `Full test`() {
        val interaction = mockk<Interaction> {
            every { userLocale } returns DiscordLocale.FRENCH
            every { guildLocale } returns DiscordLocale.FRENCH
        }
        val whatsTheFoxDoing = messageSourceFactory
            .create(interaction)
            .whatsTheFoxDoing("jumps")

        assertEquals("The fox quickly jumps", whatsTheFoxDoing)
    }
}
