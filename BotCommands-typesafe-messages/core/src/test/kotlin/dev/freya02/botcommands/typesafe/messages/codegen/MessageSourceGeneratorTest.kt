package dev.freya02.botcommands.typesafe.messages.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.exceptions.*
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator.instantiate
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageSourceGeneratorTest {

    abstract class SourceAsAbstractClass : IMessageSource

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
        assertThrows<IllegalMessageSourceClassTypeException> {
            MessageSourceGenerator.create(SourceAsAbstractClass::class)
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
        assertThrows<AbstractMessageSourceMethodException> {
            MessageSourceGenerator.create(SourceWithoutAnnotationOnAbstract::class)
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
        assertThrows<IllegalMessageSourceReturnTypeException> {
            MessageSourceGenerator.create(SourceWithAbstractWithDiffReturnType::class)
        }
    }

    @Test
    fun `Generate IMessageSource with concrete method returning non-String`() {
        assertDoesNotThrow {
            MessageSourceGenerator.create(SourceWithConcreteWithDiffReturnType::class)
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
        assertThrows<UnsupportedOptionalParameterException> {
            MessageSourceGenerator.create(SourceWithOptionalArg::class)
        }
    }

    @Test
    fun `Cannot generate IMessageSource with suspend functions`() {
        assertThrows<UnsupportedSuspendFunctionException> {
            MessageSourceGenerator.create(SourceWithSuspendFunction::class)
        }
    }

    @Test
    fun `Cannot generate IMessageSource with nullable parameters`() {
        assertThrows<UnsupportedNullableParameterException> {
            MessageSourceGenerator.create(SourceWithNullableArg::class)
        }
    }

    private fun <T : IMessageSource> createAndInstantiate(
        sourceType: KClass<T>,
        localizationContext: LocalizationContext,
    ): T {
        return instantiate(MessageSourceGenerator.create(sourceType), localizationContext)
    }
}
