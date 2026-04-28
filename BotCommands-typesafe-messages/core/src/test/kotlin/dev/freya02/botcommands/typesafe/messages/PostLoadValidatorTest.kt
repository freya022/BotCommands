package dev.freya02.botcommands.typesafe.messages

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.exceptions.NoSuchBundleException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.NoSuchTemplateKeyException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnmappedParameterException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnmappedTemplateArgumentException
import dev.freya02.botcommands.typesafe.messages.internal.PostLoadValidator
import dev.freya02.botcommands.typesafe.messages.internal.TextCommandLocaleProviderAdapter
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceFactoryGenerator
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.Test
import kotlin.test.assertTrue

class PostLoadValidatorTest {

    interface FactoryWithWrongBundle : IMessageSourceFactory<FactoryWithWrongBundle.Source> {
        interface Source : IMessageSource
    }

    interface FactoryWithUnavailableDiscordLocale : IMessageSourceFactory<FactoryWithUnavailableDiscordLocale.Source> {
        interface Source : IMessageSource
    }

    interface FactoryWithUnavailableLocale : IMessageSourceFactory<FactoryWithUnavailableLocale.Source> {
        interface Source : IMessageSource
    }

    interface Factory : IMessageSourceFactory<Factory.Source> {

        interface Source : IMessageSource {

            @LocalizedContent("factory.source.key")
            fun test(): String
        }
    }

    interface FactoryWithSourceWithUnmappedParameter : IMessageSourceFactory<FactoryWithSourceWithUnmappedParameter.Source> {

        interface Source : IMessageSource {

            @LocalizedContent("factory.source.key")
            fun test(unknownArg: String): String
        }
    }

    interface FactoryWithSourceWithUnmappedTemplateArg : IMessageSourceFactory<FactoryWithSourceWithUnmappedTemplateArg.Source> {

        interface Source : IMessageSource {

            @LocalizedContent("factory.source.key")
            fun test(): String
        }
    }

    interface FactoryWithSourceWithUnmappedLocalizedParameter : IMessageSourceFactory<FactoryWithSourceWithUnmappedLocalizedParameter.Source> {

        interface Source : IMessageSource {

            @LocalizedContent("factory.source.key")
            fun test(arg1: String): String
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
            every { getServiceOrNull<TextCommandLocaleProviderAdapter>() } returns null
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider(
                bundleName = expectedBundleName,
                discordLocales = emptySet(),
                locales = emptySet(),
                ignoreEmptyLocales = true,
                sourceFactoryType = FactoryWithWrongBundle::class
            ).get(context)
        )

        assertThrows<NoSuchBundleException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        verify(exactly = 1) { localizationService.getInstance(expectedBundleName, Locale.ROOT) }
    }

    @Test
    fun `Validates discord localized bundles exists`() {
        val expectedBundleName = "bundle"

        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance(expectedBundleName, Locale.ROOT) } returns mockk()
            every { getInstance(expectedBundleName, Locale.FRENCH) } returns null
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getServiceOrNull<TextCommandLocaleProviderAdapter>() } returns null
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider(
                bundleName = expectedBundleName,
                discordLocales = setOf(DiscordLocale.FRENCH),
                locales = emptySet(),
                ignoreEmptyLocales = false,
                sourceFactoryType = FactoryWithUnavailableDiscordLocale::class
            ).get(context)
        )

        val exception = assertThrows<NoSuchBundleException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        assertTrue("No localized bundle named" in exception.message!!)

        verify(exactly = 1) { localizationService.getInstance(expectedBundleName, Locale.ROOT) }
        verify(exactly = 1) { localizationService.getInstance(expectedBundleName, Locale.FRENCH) }
    }

    @Test
    fun `Validates localized bundles exists`() {
        val expectedBundleName = "bundle"

        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance(expectedBundleName, Locale.ROOT) } returns mockk()
            every { getInstance(expectedBundleName, Locale.FRENCH) } returns null
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getServiceOrNull<TextCommandLocaleProviderAdapter>() } returns null
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider(
                bundleName = expectedBundleName,
                discordLocales = emptySet(),
                locales = setOf(Locale.FRENCH),
                ignoreEmptyLocales = false,
                sourceFactoryType = FactoryWithUnavailableLocale::class
            ).get(context)
        )

        val exception = assertThrows<NoSuchBundleException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        assertTrue("No localized bundle named" in exception.message!!)

        verify(exactly = 1) { localizationService.getInstance(expectedBundleName, Locale.ROOT) }
        verify(exactly = 1) { localizationService.getInstance(expectedBundleName, Locale.FRENCH) }
    }

    @Test
    fun `Validates root bundle contains template key`() {
        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance(any(), any())!![any()] } returns null
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getServiceOrNull<TextCommandLocaleProviderAdapter>() } returns null
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider(
                bundleName = "bundle",
                discordLocales = emptySet(),
                locales = emptySet(),
                ignoreEmptyLocales = true,
                sourceFactoryType = Factory::class
            ).get(context)
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
            every { getServiceOrNull<TextCommandLocaleProviderAdapter>() } returns null
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider(
                bundleName = "bundle",
                discordLocales = emptySet(),
                locales = emptySet(),
                ignoreEmptyLocales = true,
                sourceFactoryType = FactoryWithSourceWithUnmappedParameter::class
            ).get(context)
        )

        assertThrows<UnmappedParameterException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        verify(exactly = 1) { localizationService.getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments }
    }

    @Test
    fun `Validates parameters matches localized bundle template arguments`() {
        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments } returns listOf(
                mockk { every { argumentName } returns "arg1" }
            )
            every { getInstance("bundle", Locale.FRENCH)!!["factory.source.key"]!!.arguments } returns emptyList()
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getServiceOrNull<TextCommandLocaleProviderAdapter>() } returns null
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider(
                bundleName = "bundle",
                discordLocales = emptySet(),
                locales = setOf(Locale.FRENCH),
                ignoreEmptyLocales = false,
                sourceFactoryType = FactoryWithSourceWithUnmappedLocalizedParameter::class
            ).get(context)
        )

        val exception = assertThrows<UnmappedParameterException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        assertTrue("from bundle 'bundle' with locale" in exception.message!!)

        verify(exactly = 1) { localizationService.getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments }
        verify(exactly = 1) { localizationService.getInstance("bundle", Locale.FRENCH)!!["factory.source.key"]!!.arguments }
    }

    @Test
    fun `Validates root bundle template arguments matches parameters`() {
        val event = mockk<PostLoadEvent>()
        val localizationService = mockk<LocalizationService> {
            every { getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments } returns listOf(mockk { every { argumentName } returns "arg_name"})
        }
        val context = mockk<BContext> {
            every { getService<LocalizationService>() } returns localizationService
            every { getServiceOrNull<TextCommandLocaleProviderAdapter>() } returns null
            every { getService<GuildLocaleProvider>() } returns mockk()
            every { getService<UserLocaleProvider>() } returns mockk()
        }
        val factories = listOf<IMessageSourceFactory<*>>(
            MessageSourceFactoryGenerator.createProvider(
                bundleName = "bundle",
                discordLocales = emptySet(),
                locales = emptySet(),
                ignoreEmptyLocales = true,
                sourceFactoryType = FactoryWithSourceWithUnmappedTemplateArg::class
            ).get(context)
        )

        assertThrows<UnmappedTemplateArgumentException> {
            PostLoadValidator.onPostLoad(event, localizationService, factories)
        }

        verify(exactly = 1) { localizationService.getInstance("bundle", Locale.ROOT)!!["factory.source.key"]!!.arguments }
    }
}
