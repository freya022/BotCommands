package dev.freya02.botcommands.typesafe.messages.integration

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.DefaultLocalizationMap
import io.github.freya022.botcommands.api.localization.DefaultLocalizationTemplate
import io.github.freya022.botcommands.api.localization.LocalizationMap
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultIntegrationTest {

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

    @BService
    class FakeBot : JDAService() {

        override val intents: Set<GatewayIntent> = emptySet()
        override val cacheFlags: Set<CacheFlag> = emptySet()

        override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {}
    }

    @BService
    class MyBundleReader(
        private val context: BContext,
    ) : LocalizationMapReader {

        override fun readLocalizationMap(request: LocalizationMapRequest): LocalizationMap? {
            if (request.baseName != "myBundle") return null

            return DefaultLocalizationMap(
                request.requestedLocale, mapOf(
                    "whats.the.fox.doing" to DefaultLocalizationTemplate(
                        context,
                        "The fox quickly {action}",
                        request.requestedLocale
                    ),
                )
            )
        }
    }

    interface MyMessageSource : IMessageSource {

        @LocalizedContent("whats.the.fox.doing")
        fun whatsTheFoxDoing(action: String): String
    }

    @MessageSourceFactory(bundleName = "myBundle", ignoreEmptyLocales = true)
    interface MyMessageSourceFactory : IMessageSourceFactory<MyMessageSource>
}
