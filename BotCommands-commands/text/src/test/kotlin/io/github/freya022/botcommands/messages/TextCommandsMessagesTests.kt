package io.github.freya022.botcommands.messages

import dev.freya02.botcommands.helpers.AbstractIntegrationTest
import io.github.freya022.botcommands.api.commands.text.messages.DefaultTextCommandsMessagesFactory
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessages
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.config.registerServiceSupplier
import io.github.freya022.botcommands.api.core.messages.exceptions.MissingMessageTemplateException
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import java.util.*
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.fail

class TextCommandsMessagesTests : AbstractIntegrationTest() {

    @Test
    fun `All messages have defaults`() {
        val context = createTest {
            services {
                // Override the autoconfiguration so we don't unexpectedly use a different implementation
                registerServiceSupplier<DefaultTextCommandsMessagesFactory>(
                    additionalTypes = setOf(
                        TextCommandsMessagesFactory::class,
                    )
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

        val missingTests =
            TextCommandsMessages::class.java.declaredMethods.mapTo(hashSetOf()) { it.name } - methodCalls.keys
        if (missingTests.isNotEmpty()) {
            fail("The following methods are missing tests:\n" + missingTests.joinAsList())
        }

        val methodsMissingTemplate: MutableList<String> = arrayListOf()
        methodCalls.values.forEach { methodCall ->
            templatePathSlot.clear()
            try {
                methodCall()
            } catch (_: MissingMessageTemplateException) {
                methodsMissingTemplate += templatePathSlot.captured
            }
        }

        if (methodsMissingTemplate.isNotEmpty()) {
            fail("The following template keys are missing default translations:\n" + methodsMissingTemplate.joinAsList())
        }
    }

    private fun <F : KFunction<MessageCreateData>> methodCall(
        callableRef: F,
        executor: F.() -> Unit,
    ): Pair<String, () -> Unit> {
        return callableRef.name to { executor(callableRef) }
    }
}
