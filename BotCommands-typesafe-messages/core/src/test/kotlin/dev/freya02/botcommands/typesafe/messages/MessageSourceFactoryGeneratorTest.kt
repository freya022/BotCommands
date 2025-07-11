package dev.freya02.botcommands.typesafe.messages

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.exceptions.AbstractMessageSourceFactoryMethodException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.IllegalMessageSourceFactoryClassTypeException
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceFactoryGenerator
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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

    @Test
    fun `Generate IMessageSourceFactory`() {
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns mockk()
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }

        mockkObject(MessageSourceGenerator)
        every { MessageSourceGenerator.create(any()) } returns mockk()

        MessageSourceFactoryGenerator.createFactory(
            context = context,
            annotation = MessageSourceFactory("testBundle"),
            sourceFactoryType = Factory::class,
            sourceType = IMessageSource::class
        )
    }

    @Test
    fun `Cannot generate IMessageSourceFactory with abstract method`() {
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns mockk()
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }

        mockkObject(MessageSourceGenerator)
        every { MessageSourceGenerator.create(any()) } returns mockk()

        assertThrows<AbstractMessageSourceFactoryMethodException> {
            MessageSourceFactoryGenerator.createFactory(
                context = context,
                annotation = MessageSourceFactory("testBundle"),
                sourceFactoryType = FactoryWithoutAnnotationOnAbstract::class,
                sourceType = IMessageSource::class
            )
        }
    }

    @Test
    fun `Generate IMessageSourceFactory with concrete method`() {
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns mockk()
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }

        mockkObject(MessageSourceGenerator)
        every { MessageSourceGenerator.create(any()) } returns mockk()

        MessageSourceFactoryGenerator.createFactory(
            context = context,
            annotation = MessageSourceFactory("testBundle"),
            sourceFactoryType = FactoryWithoutAnnotationOnConcrete::class,
            sourceType = IMessageSource::class
        )
    }

    @Test
    fun `Cannot generate IMessageSourceFactory as abstract class`() {
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns mockk()
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }

        mockkObject(MessageSourceGenerator)
        every { MessageSourceGenerator.create(any()) } returns mockk()

        assertThrows<IllegalMessageSourceFactoryClassTypeException> {
            MessageSourceFactoryGenerator.createFactory(
                context = context,
                annotation = MessageSourceFactory("testBundle"),
                sourceFactoryType = SourceFactoryAsAbstractClass::class,
                sourceType = IMessageSource::class,
            )
        }
    }
}
