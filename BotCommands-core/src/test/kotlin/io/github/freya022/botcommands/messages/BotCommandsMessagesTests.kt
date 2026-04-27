package io.github.freya022.botcommands.messages

import dev.freya02.botcommands.helpers.AbstractMessagesTests
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.config.registerServiceSupplier
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessages
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.messages.DefaultBotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.getService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import java.time.Instant
import java.util.*
import kotlin.test.Test

class BotCommandsMessagesTests : AbstractMessagesTests() {

    @Test
    fun `All messages have defaults`() {
        val context = createTest {
            services {
                // Override the autoconfiguration so we don't unexpectedly use a different implementation
                registerServiceSupplier<DefaultBotCommandsMessagesFactory>(
                    additionalTypes = setOf(BotCommandsMessagesFactory::class),
                ) { context ->
                    DefaultBotCommandsMessagesFactory(
                        context.getService(),
                        context.getService(),
                        context.getService(),
                        context.getService(),
                    )
                }
            }
        }

        val templatePathSlot = slot<String>()
        val messages = spyk(context.getService<DefaultBotCommandsMessagesFactory>().get(Locale.ROOT)) {
            every { this@spyk["getLocalizationTemplate"](capture(templatePathSlot)) } answers { callOriginal() }
        }

        val methodCalls = mapOf(
            methodCall(messages::uncaughtException) { this(mockk()) },
            methodCall(messages::missingUserPermissions) { this(mockk(), emptySet()) },
            methodCall(messages::missingBotPermissions) { this(mockk(), emptySet()) },
            methodCall(messages::userRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::channelRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::guildRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::applicationCommandsNotAvailable) { this(mockk()) },
            methodCall(messages::resolverChannelNotFound) { this(mockk(), 0) },
            methodCall(messages::resolverChannelMissingAccess) { this(mockk(), 0) },
            methodCall(messages::resolverUserNotFound) { this(mockk(), 0) },
            methodCall(messages::slashCommandUnresolvableOption) {
                val option = mockk<SlashCommandOption> {
                    every { discordName } returns "discord_name"
                }
                this(mockk(), option)
            },
            methodCall(messages::componentNotAllowed) { this(mockk()) },
            methodCall(messages::componentExpired) { this(mockk()) },
            methodCall(messages::modalExpired) { this(mockk()) },
        )

        checkMissingTests(BotCommandsMessages::class.java, methodCalls)

        checkMissingTemplates(methodCalls, templatePathSlot)
    }
}
