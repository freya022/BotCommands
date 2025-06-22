@file:Suppress("DEPRECATION", "removal")

package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.registerServiceSupplier
import io.github.freya022.botcommands.api.core.replies.BuiltinReplies
import io.github.freya022.botcommands.api.core.replies.BuiltinRepliesFactory
import io.github.freya022.botcommands.api.core.replies.DefaultBuiltinRepliesFactory
import io.github.freya022.botcommands.api.core.replies.exceptions.MissingReplyTemplateException
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.localization.DefaultMessages
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.framework.utils.createTest
import io.github.freya022.botcommands.internal.core.replies.BuiltinRepliesFactoryProvider
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

class BuiltinRepliesTests {

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

        assertIsNot<DefaultBuiltinRepliesFactory>(context.getService<BuiltinRepliesFactory>())
    }

    @Test
    fun `Adapter is used when custom DefaultMessages JSON exists`() {
        val context = BotCommands.createTest {
            services {
                registerServiceSupplier<BuiltinRepliesFactoryProvider> {
                    mockk {
                        every { builtinRepliesFactory(any(), any(), any(), any(), any()) } answers { callOriginal() }

                        every { this@mockk["hasCustomDefaultMessages"]() } returns true
                    }
                }
            }
        }

        assertIsNot<DefaultBuiltinRepliesFactory>(context.getService<BuiltinRepliesFactory>())
    }

    @Test
    fun `All built-in replies have defaults`() {
        val context = BotCommands.createTest {
            services {
                // Override the autoconfiguration so we don't unexpectedly use a different implementation
                registerServiceSupplier<DefaultBuiltinRepliesFactory>(additionalTypes = setOf(BuiltinRepliesFactory::class)) { context ->
                    DefaultBuiltinRepliesFactory(
                        context.getService(),
                        context.getService(),
                        context.getService(),
                        context.getService()
                    )
                }
            }
        }

        val templatePathSlot = slot<String>()
        val builtinReplies = spyk(context.getService<DefaultBuiltinRepliesFactory>().get(Locale.ROOT), recordPrivateCalls = true) {
            every { this@spyk["getLocalizationTemplate"](capture(templatePathSlot)) } answers { callOriginal() }
        }

        val methodCalls = mapOf(
            methodCall(builtinReplies::uncaughtException) { this(mockk()) },
            methodCall(builtinReplies::missingUserPermissions) { this(mockk(), emptySet()) },
            methodCall(builtinReplies::missingBotPermissions) { this(mockk(), emptySet()) },
            methodCall(builtinReplies::ownerOnly) { this(mockk()) },
            methodCall(builtinReplies::userRateLimited) { this(mockk(), Instant.now()) },
            methodCall(builtinReplies::channelRateLimited) { this(mockk(), Instant.now()) },
            methodCall(builtinReplies::guildRateLimited) { this(mockk(), Instant.now()) },
            methodCall(builtinReplies::applicationCommandsNotAvailable) { this(mockk()) },
            methodCall(builtinReplies::commandNotFound) { this(mockk(), emptySet()) },
            methodCall(builtinReplies::resolverChannelNotFound) { this(mockk(), 0) },
            methodCall(builtinReplies::resolverChannelMissingAccess) { this(mockk(), 0) },
            methodCall(builtinReplies::resolverUserNotFound) { this(mockk(), 0) },
            methodCall(builtinReplies::slashCommandUnresolvableOption) {
                this(mockk(), mockk {
                    every { discordName } returns "discord_name"
                })
            },
            methodCall(builtinReplies::closedDirectMessages) { this(mockk()) },
            methodCall(builtinReplies::nsfwOnly) { this(mockk()) },
            methodCall(builtinReplies::componentNotAllowed) { this(mockk()) },
            methodCall(builtinReplies::componentExpired) { this(mockk()) },
            methodCall(builtinReplies::modalExpired) { this(mockk()) },
        )

        val missingTests = BuiltinReplies::class.java.declaredMethods.mapTo(hashSetOf()) { it.name } - methodCalls.keys
        if (missingTests.isNotEmpty()) {
            fail("The following methods are missing tests:\n" + missingTests.joinAsList())
        }

        val methodsMissingTemplate: MutableList<String> = arrayListOf()
        methodCalls.values.forEach { methodCall ->
            templatePathSlot.clear()
            try {
                methodCall()
            } catch (_: MissingReplyTemplateException) {
                methodsMissingTemplate += templatePathSlot.captured
            }
        }

        if (methodsMissingTemplate.isNotEmpty()) {
            fail("The following template keys are missing default translations:\n" + methodsMissingTemplate.joinAsList())
        }
    }

    private fun <F : KFunction<MessageCreateData>> methodCall(
        callableRef: F,
        executor: F.() -> Unit
    ): Pair<String, () -> Unit> {
        return callableRef.name to { executor(callableRef) }
    }
}