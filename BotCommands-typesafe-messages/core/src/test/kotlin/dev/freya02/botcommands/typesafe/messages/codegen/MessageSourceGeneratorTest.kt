package dev.freya02.botcommands.typesafe.messages.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.LocalePreference
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.annotations.PreferLocale
import dev.freya02.botcommands.typesafe.messages.api.exceptions.*
import dev.freya02.botcommands.typesafe.messages.internal.MessageSourceContext
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator.instantiate
import io.github.freya022.botcommands.api.localization.Localization
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.*
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

    interface SourceWithDiscordLocale: IMessageSource {

        @LocalizedContent("SourceWithDiscordLocale.key")
        fun test(locale: DiscordLocale): String
    }

    interface SourceWithNullableDiscordLocale: IMessageSource {

        @LocalizedContent("SourceWithNullableDiscordLocale.key")
        fun test(locale: DiscordLocale?): String
    }

    interface SourceWithLocale: IMessageSource {

        @LocalizedContent("SourceWithLocale.key")
        fun test(locale: Locale): String
    }

    interface SourceWithNullableLocale: IMessageSource {

        @LocalizedContent("SourceWithNullableLocale.key")
        fun test(locale: Locale?): String
    }

    interface SourceUsingGuildLocale: IMessageSource {

        @PreferLocale(LocalePreference.GUILD)
        @LocalizedContent("SourceUsingGuildLocale.key")
        fun test(): String
    }

    @PreferLocale(LocalePreference.GUILD)
    interface SourceUsingGuildLocaleFromClass: IMessageSource {

        @LocalizedContent("SourceUsingGuildLocaleFromClass.key")
        fun test(): String
    }

    interface SourcePreferringGuildLocaleIsOverriddenByRequiredLocale: IMessageSource {

        @PreferLocale(LocalePreference.GUILD)
        @LocalizedContent("SourcePreferringGuildLocaleIsOverriddenByRequiredLocale.key")
        fun test(locale: Locale): String
    }

    interface SourcePreferredGuildLocaleOverridesNullLocale: IMessageSource {

        @PreferLocale(LocalePreference.GUILD)
        @LocalizedContent("SourcePreferredGuildLocaleOverridesNullLocale.key")
        fun test(locale: Locale?): String
    }

    @Test
    fun `Cannot generate IMessageSource as abstract class`() {
        assertThrows<IllegalMessageSourceClassTypeException> {
            MessageSourceGenerator.create(SourceAsAbstractClass::class)
        }
    }

    @Test
    fun `Generate IMessageSource without params`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizePreferringUser(any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithoutArgs::class, messageSourceContext)

        source.test()
        verify(exactly = 1) { messageSourceContext.localizePreferringUser("SourceWithoutArgs.key") }
    }

    @Test
    fun `Generate IMessageSource with params`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizePreferringUser(any<String>(), any<Localization.Entry>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithArgs::class, messageSourceContext)

        source.test("42")
        verify(exactly = 1) { messageSourceContext.localizePreferringUser("SourceWithArgs.key", Localization.Entry("string", "42")) }
    }

    @Test
    fun `Generate IMessageSource with primitive params`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizePreferringUser(any<String>(), any<Localization.Entry>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithPrimitiveArgs::class, messageSourceContext)

        source.test(42)
        verify(exactly = 1) { messageSourceContext.localizePreferringUser("SourceWithPrimitiveArgs.key", Localization.Entry("integer", 42)) }
    }

    @Test
    fun `Cannot generate IMessageSource without annotation on abstract method`() {
        assertThrows<AbstractMessageSourceMethodException> {
            MessageSourceGenerator.create(SourceWithoutAnnotationOnAbstract::class)
        }
    }

    @Test
    fun `Generate IMessageSource without annotation on concrete method`() {
        val messageSourceContext = mockk<MessageSourceContext>()
        val source = createAndInstantiate(SourceWithoutAnnotationOnConcrete::class, messageSourceContext)
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
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizePreferringUser(any<String>(), any<Localization.Entry>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithCamelCaseArg::class, messageSourceContext)
        source.test("arg")

        verify(exactly = 1) { messageSourceContext.localizePreferringUser("SourceWithCamelCaseArg.key", Localization.Entry("my_arg", "arg")) }
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

    @Test
    fun `Have DiscordLocale as first argument`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizeWith(DiscordLocale.FRENCH, any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithDiscordLocale::class, messageSourceContext)
        source.test(DiscordLocale.FRENCH)

        verify(exactly = 1) { messageSourceContext.localizeWith(DiscordLocale.FRENCH, "SourceWithDiscordLocale.key") }
    }

    @Test
    fun `Have null DiscordLocale as first argument`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizePreferringUser(any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithNullableDiscordLocale::class, messageSourceContext)
        source.test(null)

        // Check it calls the method which uses the best locale
        verify(exactly = 1) { messageSourceContext.localizePreferringUser("SourceWithNullableDiscordLocale.key") }
    }

    @Test
    fun `Have Locale as first argument`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizeWith(Locale.FRENCH, any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithLocale::class, messageSourceContext)
        source.test(Locale.FRENCH)

        verify(exactly = 1) { messageSourceContext.localizeWith(Locale.FRENCH, "SourceWithLocale.key") }
    }

    @Test
    fun `Have null Locale as first argument`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizePreferringUser(any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceWithNullableLocale::class, messageSourceContext)
        source.test(null)

        // Check it calls the method which uses the best locale
        verify(exactly = 1) { messageSourceContext.localizePreferringUser("SourceWithNullableLocale.key") }
    }

    @Test
    fun `Prefer guild locale`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizeWithGuild(any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceUsingGuildLocale::class, messageSourceContext)
        source.test()

        // Check it calls the method which uses the best locale
        verify(exactly = 1) { messageSourceContext.localizeWithGuild("SourceUsingGuildLocale.key") }
    }

    @Test
    fun `Prefer guild locale from class`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizeWithGuild(any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourceUsingGuildLocaleFromClass::class, messageSourceContext)
        source.test()

        // Check it calls the method which uses the best locale
        verify(exactly = 1) { messageSourceContext.localizeWithGuild("SourceUsingGuildLocaleFromClass.key") }
    }

    @Test
    fun `Preferred locale is overridden by required locale`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizeWith(any<Locale>(), any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourcePreferringGuildLocaleIsOverriddenByRequiredLocale::class, messageSourceContext)
        source.test(Locale.FRENCH)

        // Check it calls the method which uses the best locale
        verify(exactly = 1) { messageSourceContext.localizeWith(Locale.FRENCH, "SourcePreferringGuildLocaleIsOverriddenByRequiredLocale.key") }
    }

    @Test
    fun `Preferred locale overrides null locale`() {
        val messageSourceContext = mockk<MessageSourceContext> {
            every { localizeWithGuild(any<String>()) } returns "expected"
        }
        val source = createAndInstantiate(SourcePreferredGuildLocaleOverridesNullLocale::class, messageSourceContext)
        source.test(null)

        // Check it calls the method which uses the best locale
        verify(exactly = 1) { messageSourceContext.localizeWithGuild("SourcePreferredGuildLocaleOverridesNullLocale.key") }
    }

    private fun <T : IMessageSource> createAndInstantiate(
        sourceType: KClass<T>,
        messageSourceContext: MessageSourceContext,
    ): T {
        return instantiate(MessageSourceGenerator.create(sourceType), messageSourceContext)
    }
}
