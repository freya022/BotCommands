package dev.freya02.botcommands.typesafe.messages.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.exceptions.AbstractMessageSourceFactoryMethodException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.IllegalMessageSourceFactoryClassTypeException
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceFactoryGenerator
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class MessageSourceFactoryGeneratorTest {

    interface Factory : IMessageSourceFactory<IMessageSource>

    interface FactoryWithoutAnnotationOnAbstract : IMessageSourceFactory<IMessageSource> {

        fun test(): String
    }

    interface FactoryWithoutAnnotationOnConcrete : IMessageSourceFactory<IMessageSource> {

        fun test(): String = "test"
    }

    abstract class SourceFactoryAsAbstractClass : IMessageSourceFactory<IMessageSource>

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Generate IMessageSourceFactory`() {
        mockkObject(MessageSourceGenerator)
        every { MessageSourceGenerator.create(any()) } returns mockk()

        MessageSourceFactoryGenerator.createProvider(
            bundleName = "testBundle",
            discordLocales = emptySet(),
            locales = emptySet(),
            ignoreEmptyLocales = true,
            sourceFactoryType = Factory::class,
        )

        // Make sure the factory generator also triggers MessageSourceGenerator checks
        verify(exactly = 1) { MessageSourceGenerator.create(any()) }
    }

    @Test
    fun `Cannot generate IMessageSourceFactory with abstract method`() {
        mockkObject(MessageSourceGenerator)
        every { MessageSourceGenerator.create(any()) } returns mockk()

        assertThrows<AbstractMessageSourceFactoryMethodException> {
            MessageSourceFactoryGenerator.createProvider(
                bundleName = "testBundle",
                discordLocales = emptySet(),
                locales = emptySet(),
                ignoreEmptyLocales = true,
                sourceFactoryType = FactoryWithoutAnnotationOnAbstract::class,
            )
        }
    }

    @Test
    fun `Generate IMessageSourceFactory with concrete method`() {
        mockkObject(MessageSourceGenerator)
        every { MessageSourceGenerator.create(any()) } returns mockk()

        MessageSourceFactoryGenerator.createProvider(
            bundleName = "testBundle",
            discordLocales = emptySet(),
            locales = emptySet(),
            ignoreEmptyLocales = true,
            sourceFactoryType = FactoryWithoutAnnotationOnConcrete::class,
        )
    }

    @Test
    fun `Cannot generate IMessageSourceFactory as abstract class`() {
        mockkObject(MessageSourceGenerator)
        every { MessageSourceGenerator.create(any()) } returns mockk()

        assertThrows<IllegalMessageSourceFactoryClassTypeException> {
            MessageSourceFactoryGenerator.createProvider(
                bundleName = "testBundle",
                discordLocales = emptySet(),
                locales = emptySet(),
                ignoreEmptyLocales = true,
                sourceFactoryType = SourceFactoryAsAbstractClass::class,
            )
        }
    }
}
