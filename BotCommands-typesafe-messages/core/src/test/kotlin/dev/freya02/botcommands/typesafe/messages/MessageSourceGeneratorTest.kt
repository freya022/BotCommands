package dev.freya02.botcommands.typesafe.messages

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.exceptions.*
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceFactoryGenerator
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator.instantiate
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageSourceGeneratorTest {

    interface SourceFactoryWithAbstractClassSource : IMessageSourceFactory<SourceFactoryWithAbstractClassSource.SourceAsAbstractClass> {
        abstract class SourceAsAbstractClass : IMessageSource
    }

    interface SourceWithoutArgs : IMessageSource {

        @LocalizedContent("SourceWithoutArgs.key")
        fun test(): String
    }

    interface SourceWithArgs : IMessageSource {

        @LocalizedContent("SourceWithArgs.key")
        fun test(string: String): String
    }

    interface SourceWithPrimitiveArgs : IMessageSource {

        @LocalizedContent("SourceWithPrimitiveArgs.key")
        fun test(integer: Int): String
    }

    interface SourceWithoutAnnotationOnAbstract : IMessageSource {

        fun test(): String
    }

    interface SourceWithoutAnnotationOnConcrete : IMessageSource {

        fun test(): String = "test"
    }

    interface SourceWithAbstractWithDiffReturnType : IMessageSource {

        @LocalizedContent("SourceWithAbstractWithDiffReturnType.key")
        fun test()
    }

    interface SourceWithConcreteWithDiffReturnType : IMessageSource {

        @LocalizedContent("SourceWithConcreteWithDiffReturnType.key")
        fun test() { }
    }

    interface SourceWithCamelCaseArg : IMessageSource {

        @LocalizedContent("SourceWithCamelCaseArg.key")
        fun test(myArg: String): String
    }

    interface SourceWithOptionalArg: IMessageSource {

        @LocalizedContent("SourceWithOptionalArg.key")
        fun test(myArg: String = "test"): String
    }

    interface SourceWithSuspendFunction: IMessageSource {

        @LocalizedContent("SourceWithSuspendFunction.key")
        suspend fun test(): String
    }

    interface SourceWithNullableArg: IMessageSource {

        @LocalizedContent("SourceWithNullableArg.key")
        fun test(arg: String?): String
    }

    @Test
    fun `Cannot generate IMessageSource as abstract class`() {
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns mockk()
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        assertThrows<IllegalMessageSourceClassTypeException> {
            MessageSourceFactoryGenerator.createFactory(
                context = context,
                annotation = MessageSourceFactory("testBundle"),
                sourceFactoryType = SourceFactoryWithAbstractClassSource::class,
                sourceType = SourceFactoryWithAbstractClassSource.SourceAsAbstractClass::class,
            )
        }
    }

    @Test
    fun `Generate IMessageSource without params`() {
        val localizationContext = mockk<LocalizationContext> {
            every { localize(any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithoutArgs::class, localizationContext)

        source.test()
        verify(exactly = 1) { localizationContext.localize("SourceWithoutArgs.key") }
    }

    @Test
    fun `Generate IMessageSource with params`() {
        val localizationContext = mockk<LocalizationContext> {
            every { localize(any<String>(), any<Localization.Entry>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithArgs::class, localizationContext)

        source.test("42")
        verify(exactly = 1) { localizationContext.localize("SourceWithArgs.key", Localization.Entry("string", "42")) }
    }

    @Test
    fun `Generate IMessageSource with primitive params`() {
        val localizationContext = mockk<LocalizationContext> {
            every { localize(any<String>(), any<Localization.Entry>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithPrimitiveArgs::class, localizationContext)

        source.test(42)
        verify(exactly = 1) { localizationContext.localize("SourceWithPrimitiveArgs.key", Localization.Entry("integer", 42)) }
    }

    @Test
    fun `Cannot generate IMessageSource without annotation on abstract method`() {
        val localizationContext = mockk<LocalizationContext>()
        assertThrows<AbstractMessageSourceMethodException> {
            createAndInstantiate(SourceWithoutAnnotationOnAbstract::class, localizationContext)
        }
    }

    @Test
    fun `Generate IMessageSource without annotation on concrete method`() {
        val localizationContext = mockk<LocalizationContext>()
        val source = createAndInstantiate(SourceWithoutAnnotationOnConcrete::class, localizationContext)
        assertEquals("test", source.test())
    }

    @Test
    fun `Cannot generate IMessageSource with abstract method returning non-String`() {
        val localizationContext = mockk<LocalizationContext>()
        assertThrows<IllegalMessageSourceReturnTypeException> {
            createAndInstantiate(SourceWithAbstractWithDiffReturnType::class, localizationContext)
        }
    }

    @Test
    fun `Generate IMessageSource with concrete method returning non-String`() {
        val localizationContext = mockk<LocalizationContext>()
        assertDoesNotThrow {
            val source = createAndInstantiate(SourceWithConcreteWithDiffReturnType::class, localizationContext)
            source.test()
        }
    }

    @Test
    fun `Generate IMessageSource with camelCase param converts to snake_case`() {
        val localizationContext = mockk<LocalizationContext> {
            every { localize(any<String>(), any<Localization.Entry>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithCamelCaseArg::class, localizationContext)
        source.test("arg")

        verify(exactly = 1) { localizationContext.localize("SourceWithCamelCaseArg.key", Localization.Entry("my_arg", "arg")) }
    }

    @Test
    fun `Cannot generate IMessageSource with optional parameters`() {
        val localizationContext = mockk<LocalizationContext>()
        assertThrows<UnsupportedOptionalParameterException> {
            createAndInstantiate(SourceWithOptionalArg::class, localizationContext)
        }
    }

    @Test
    fun `Cannot generate IMessageSource with suspend functions`() {
        val localizationContext = mockk<LocalizationContext>()
        assertThrows<UnsupportedSuspendFunctionException> {
            createAndInstantiate(SourceWithSuspendFunction::class, localizationContext)
        }
    }

    @Test
    fun `Cannot generate IMessageSource with nullable parameters`() {
        val localizationContext = mockk<LocalizationContext>()
        assertThrows<UnsupportedNullableParameterException> {
            createAndInstantiate(SourceWithNullableArg::class, localizationContext)
        }
    }

    private fun <T : IMessageSource> createAndInstantiate(
        sourceType: KClass<T>,
        localizationContext: LocalizationContext,
    ): T {
        return instantiate(MessageSourceGenerator.create(sourceType), localizationContext)
    }
}
