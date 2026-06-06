package io.github.freya022.botcommands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.annotations.Hidden
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.annotations.NSFW
import io.github.freya022.botcommands.api.commands.text.annotations.RequireOwner
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandManager
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.ratelimit.AnnotatedRateLimiterFactory
import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.annotations.Cooldown
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimit
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimitReference
import io.github.freya022.botcommands.internal.commands.autobuilder.RateLimitAutoBuilderHelper
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.text.autobuilder.TextCommandAutoBuilder
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.ratelimit.RateLimitContainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class TextCommandAutoBuilderTests {

    @Nested
    inner class ClassApplicableAnnotations {
        @Test
        fun `@NSFW @Hidden @RequireOwner`() {
            @NSFW
            @Hidden
            @RequireOwner
            class TestCommand {
                @JDATextCommandVariation(path = ["test"])
                fun onTextTest(@Suppress("unused") event: BaseCommandEvent) {}
            }

            val commandInfo = getBuiltCommand(TestCommand(), TestCommand::onTextTest)
            assertTrue(commandInfo.hidden, "Command should be hidden because of the class annotation")
            assertTrue(commandInfo.nsfw, "Command should be hidden because of the class annotation")
            assertTrue(commandInfo.isOwnerRequired, "Command should be hidden because of the class annotation")
        }

        @Test
        fun `@Cooldown`() {
            @Cooldown(1)
            class TestCommand {
                @JDATextCommandVariation(path = ["test"])
                fun onTextTest(@Suppress("unused") event: BaseCommandEvent) {}
            }

            val commandInfo = getBuiltCommand(TestCommand(), TestCommand::onTextTest)
            assertNotNull(commandInfo.rateLimitInfo, "Command should have rate limit because of the class annotation")
        }

        @Test
        fun `@RateLimit`() {
            @RateLimit(RateLimitScope.USER /* ... */)
            class TestCommand {
                @JDATextCommandVariation(path = ["test"])
                fun onTextTest(@Suppress("unused") event: BaseCommandEvent) {}
            }

            val commandInfo = getBuiltCommand(TestCommand(), TestCommand::onTextTest)
            assertNotNull(commandInfo.rateLimitInfo, "Command should have rate limit because of the class annotation")
        }

        @Test
        fun `@RateLimitReference`() {
            @RateLimitReference("ref")
            class TestCommand {
                @JDATextCommandVariation(path = ["test"])
                fun onTextTest(@Suppress("unused") event: BaseCommandEvent) {}
            }

            val commandInfo = getBuiltCommand(TestCommand(), TestCommand::onTextTest)
            assertNotNull(commandInfo.rateLimitInfo, "Command should have rate limit because of the class annotation")
        }

        private fun getBuiltCommand(instance: Any, f: KFunction<Unit>): TextCommandInfoImpl {
            val functionAnnotationsMap = mockk<FunctionAnnotationsMap> {
                every { getWithClassAnnotation<Command, JDATextCommandVariation>() } returns listOf(ClassPathFunction(instance, f))
            }

            val serviceContainer = mockk<ServiceContainer> {
                every { getService<AnnotatedRateLimiterFactory>() } returns mockk(relaxed = true)
            }
            val autoBuilder = TextCommandAutoBuilder(mockk(), functionAnnotationsMap, serviceContainer)
            val context = mockk<BContext> {
                every { getService<RateLimitContainer>() } returns mockk(relaxed = true)
            }
            val manager = TextCommandManager(context)

            mockkObject(RateLimitAutoBuilderHelper) {
                every { RateLimitAutoBuilderHelper.readRateLimit(any(), any()) } returns mockk()
                every { RateLimitAutoBuilderHelper.readCooldown(any(), any()) } returns mockk()

                autoBuilder.declareTextCommands(manager)
            }

            return manager.textCommands.values.single()
        }
    }

    @Test
    fun `Variations sharing a path must be in the same class`() {
        class TestCommand1 {
            @JDATextCommandVariation(path = ["test"])
            fun onTextTest(@Suppress("unused") event: BaseCommandEvent) {}
        }

        class TestCommand2 {
            @JDATextCommandVariation(path = ["test"])
            fun onTextTest(@Suppress("unused") event: BaseCommandEvent) {}
        }

        val functionAnnotationsMap = mockk<FunctionAnnotationsMap> {
            every { getWithClassAnnotation<Command, JDATextCommandVariation>() } returns listOf(
                ClassPathFunction(TestCommand1(), TestCommand1::onTextTest),
                ClassPathFunction(TestCommand2(), TestCommand2::onTextTest),
            )
        }

        val ex = assertThrows<RuntimeException> { TextCommandAutoBuilder(mockk(), functionAnnotationsMap, mockk()) }
        assertContains(ex.cause?.cause?.message.orEmpty(), "All variations of text command 'test' must be in the same class")
    }
}
