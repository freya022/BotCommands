package io.github.freya022.botcommands.messages

import dev.freya02.botcommands.helpers.AbstractMessagesTests
import io.github.freya022.botcommands.api.commands.text.messages.DefaultTextCommandsMessagesFactory
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessages
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.config.registerServiceSupplier
import io.github.freya022.botcommands.api.core.service.getService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import java.time.Instant
import java.util.*
import kotlin.test.Test

class TextCommandsMessagesTests : AbstractMessagesTests() {

    @Test
    fun `All messages have defaults`() {
        val context = createTest {
            services {
                // Override the autoconfiguration so we don't unexpectedly use a different implementation
                registerServiceSupplier<DefaultTextCommandsMessagesFactory>(
                    additionalTypes = setOf(TextCommandsMessagesFactory::class),
                ) { context ->
                    DefaultTextCommandsMessagesFactory(
                        context.getService(),
                        context.getService(),
                        context.getService(),
                        context.getService(),
                    )
                }
            }
        }

        val templatePathSlot = slot<String>()
        val messages = spyk(context.getService<DefaultTextCommandsMessagesFactory>().get(Locale.ROOT)) {
            every { this@spyk["getLocalizationTemplate"](capture(templatePathSlot)) } answers { callOriginal() }
        }

        val methodCalls = mapOf(
            methodCall(messages::uncaughtException) { this(mockk()) },
            methodCall(messages::missingUserPermissions) { this(mockk(), emptySet()) },
            methodCall(messages::missingBotPermissions) { this(mockk(), emptySet()) },
            methodCall(messages::ownerOnly) { this(mockk()) },
            methodCall(messages::userRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::channelRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::guildRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::commandNotFound) { this(mockk(), emptySet()) },
            methodCall(messages::closedDirectMessages) { this(mockk()) },
            methodCall(messages::nsfwOnly) { this(mockk()) },
        )

        checkMissingTests(TextCommandsMessages::class.java, methodCalls)

        checkMissingTemplates(methodCalls, templatePathSlot)
    }
}
