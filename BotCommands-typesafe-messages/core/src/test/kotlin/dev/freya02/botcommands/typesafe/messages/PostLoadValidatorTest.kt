package dev.freya02.botcommands.typesafe.messages

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.exceptions.NoSuchBundleException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.NoSuchTemplateKeyException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnmappedParameterException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnmappedTemplateArgumentException
import dev.freya02.botcommands.typesafe.messages.internal.PostLoadValidator
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceFactoryGenerator
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.Test

class PostLoadValidatorTest {

    interface FactoryWithWrongBundle : IMessageSourceFactory<IMessageSource>

    interface Factory : IMessageSourceFactory<Factory.Source> {

        interface Source : IMessageSource {

            @LocalizedContent("factory.source.key")
            fun test(): String
        }
    }

    interface FactoryWithSourceWithUnknownParameter : IMessageSourceFactory<FactoryWithSourceWithUnknownParameter.Source> {

        interface Source : IMessageSource {

            @LocalizedContent("factory.source.key")
            fun test(unknownArg: String): String
        }
    }

    interface FactoryWithSourceWithUnknownTemplateArg : IMessageSourceFactory<FactoryWithSourceWithUnknownTemplateArg.Source> {

        interface Source : IMessageSource {

            @LocalizedContent("factory.source.key")
            fun test(): String
        }
    }

    @Test
    fun `Validates root bundle exists`() {
        val expectedBundleName = "wrongBundle"

        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance(any(), any()) } returns null
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider(expectedBundleName, FactoryWithWrongBundle::class).get(context)
        )

        assertThrows<NoSuchBundleException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        verify(exactly = 1) { localizationService.getInstance(expectedBundleName, Locale.ROOT) }
    }

    @Test
    fun `Validates root bundle contains template key`() {
        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance(any(), any())!![any()] } returns null
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider("bundle", Factory::class).get(context)
        )

        assertThrows<NoSuchTemplateKeyException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        val localization = localizationService.getInstance("bundle", Locale.ROOT)!!
        verify(exactly = 1) { localization[any<String>()] }
    }

    @Test
    fun `Validates parameters matches root bundle template arguments`() {
        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments } returns emptyList()
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider("bundle", FactoryWithSourceWithUnknownParameter::class).get(context)
        )

        assertThrows<UnmappedParameterException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        verify(exactly = 1) { localizationService.getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments }
    }

    @Test
    fun `Validates root bundle template arguments matches parameters`() {
        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments } returns listOf(mockk { every { argumentName } returns "arg_name"})
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider("bundle", FactoryWithSourceWithUnknownTemplateArg::class).get(context)
        )

        assertThrows<UnmappedTemplateArgumentException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        verify(exactly = 1) { localizationService.getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments }
    }
}
