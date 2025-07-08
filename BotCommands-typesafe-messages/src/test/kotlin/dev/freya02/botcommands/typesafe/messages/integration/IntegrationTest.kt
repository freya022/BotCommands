package dev.freya02.botcommands.typesafe.messages.integration

import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.service.getService
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationTest {

    @Test
    fun `Full test`() {
        val context = BotCommands.create {
            textCommands { enable = false }
            applicationCommands { enable = false }
            modals { enable = false }

            addClass<FakeBot>()
            addClass<MyMessageSourceFactory>()
            addClass<MyBundleReader>()
        }

        val interaction = mockk<Interaction> {
            every { userLocale } returns DiscordLocale.FRENCH
            every { guildLocale } returns DiscordLocale.FRENCH
        }
        val whatsTheFoxDoing = context.getService<MyMessageSourceFactory>()
            .create(interaction)
            .whatsTheFoxDoing("jumps")

        assertEquals("The fox quickly jumps", whatsTheFoxDoing)
    }
}
