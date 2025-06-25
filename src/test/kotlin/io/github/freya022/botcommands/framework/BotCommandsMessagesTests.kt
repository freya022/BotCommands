@file:Suppress("DEPRECATION", "removal")

package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.registerServiceSupplier
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessages
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.messages.DefaultBotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.messages.exceptions.MissingMessageTemplateException
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.localization.DefaultMessages
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.framework.utils.createTest
import io.github.freya022.botcommands.internal.core.messages.BotCommandsMessagesFactoryAutoConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import java.util.*
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertIsNot
import kotlin.test.fail

class BotCommandsMessagesTests {

    @Test
    fun `Adapter is used when custom DefaultMessagesFactory type is used`() {
        val context = BotCommands.createTest {
            services {
                registerServiceSupplier<DefaultMessagesFactory> {
                    object : DefaultMessagesFactory {
                        override fun get(locale: Locale): DefaultMessages = throw UnsupportedOperationException()
                        override fun get(event: MessageReceivedEvent) = throw UnsupportedOperationException()
                        override fun get(event: Interaction) = throw UnsupportedOperationException()
                    }
                }
            }
        }

        assertIsNot<DefaultBotCommandsMessagesFactory>(context.getService<BotCommandsMessagesFactory>())
    }

    @Test
    fun `Adapter is used when custom DefaultMessages JSON exists`() {
        val context = BotCommands.createTest {
            services {
                registerServiceSupplier<BotCommandsMessagesFactoryAutoConfiguration> {
                    mockk {
                        every {
                            botCommandsMessagesFactory(any(), any(), any(), any(), any())
                        } answers { callOriginal() }

                        every { this@mockk["hasCustomDefaultMessages"]() } returns true
                    }
                }
            }
        }

        assertIsNot<DefaultBotCommandsMessagesFactory>(context.getService<BotCommandsMessagesFactory>())
    }

    @Test
    fun `All messages have defaults`() {
        val context = BotCommands.createTest {
            services {
                // Override the autoconfiguration so we don't unexpectedly use a different implementation
                registerServiceSupplier<DefaultBotCommandsMessagesFactory>(
                    additionalTypes = setOf(
                        BotCommandsMessagesFactory::class,
                    )
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
            methodCall(messages::ownerOnly) { this(mockk()) },
            methodCall(messages::userRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::channelRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::guildRateLimited) { this(mockk(), Instant.now()) },
            methodCall(messages::applicationCommandsNotAvailable) { this(mockk()) },
            methodCall(messages::commandNotFound) { this(mockk(), emptySet()) },
            methodCall(messages::resolverChannelNotFound) { this(mockk(), 0) },
            methodCall(messages::resolverChannelMissingAccess) { this(mockk(), 0) },
            methodCall(messages::resolverUserNotFound) { this(mockk(), 0) },
            methodCall(messages::slashCommandUnresolvableOption) {
                this(mockk(), mockk {
                    every { discordName } returns "discord_name"
                })
            },
            methodCall(messages::closedDirectMessages) { this(mockk()) },
            methodCall(messages::nsfwOnly) { this(mockk()) },
            methodCall(messages::componentNotAllowed) { this(mockk()) },
            methodCall(messages::componentExpired) { this(mockk()) },
            methodCall(messages::modalExpired) { this(mockk()) },
        )

        val missingTests =
            BotCommandsMessages::class.java.declaredMethods.mapTo(hashSetOf()) { it.name } - methodCalls.keys
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
