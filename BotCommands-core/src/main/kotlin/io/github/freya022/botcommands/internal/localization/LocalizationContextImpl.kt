package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.internal.utils.LocalizationUtils
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

internal class LocalizationContextImpl(
    private val localizationService: LocalizationService,
    override val localizationBundle: String,
    override val localizationPrefix: String?,
    private val _guildLocaleProvider: Lazy<Locale>?,
    private val _userLocaleProvider: Lazy<Locale>?,
) : TextLocalizationContext, AppLocalizationContext {
    private val guildLocaleProvider: Lazy<Locale>
        get() = _guildLocaleProvider ?: throwArgument("Cannot guild localize on an event which doesn't provide guild localization")

    private val userLocaleProvider: Lazy<Locale>
        get() = _userLocaleProvider ?: throwArgument("Cannot user localize on an event which doesn't provide user localization")

    private val userJavaLocale: Locale
        get() = userLocaleProvider.value

    private val guildJavaLocale: Locale
        get() = guildLocaleProvider.value

    override val userLocale: DiscordLocale
        get() = DiscordLocale.from(userJavaLocale)

    override val guildLocale: DiscordLocale
        get() = DiscordLocale.from(guildJavaLocale)

    override val effectiveLocale: DiscordLocale
        get() = when {
            _userLocaleProvider != null -> userLocale
            _guildLocaleProvider != null -> guildLocale
            else -> DiscordLocale.ENGLISH_US
        }

    init {
        // At least the root bundle must exists
        requireNotNull(localizationService.getInstance(localizationBundle, Locale.ROOT)) {
            "A root localization bundle must exist for $localizationBundle"
        }
    }

    @Deprecated("Use the Locale overload")
    override fun withGuildLocale(guildLocale: DiscordLocale?): LocalizationContextImpl =
        withGuildLocale(guildLocale?.toLocale())

    override fun withGuildLocale(guildLocale: Locale?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, guildLocale?.toProvider(), _userLocaleProvider)
    }

    @Deprecated("Use the Locale overload")
    override fun withUserLocale(userLocale: DiscordLocale?): LocalizationContextImpl =
        withUserLocale(userLocale?.toLocale())

    override fun withUserLocale(userLocale: Locale?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, _guildLocaleProvider, userLocale?.toProvider())
    }

    override fun withBundle(localizationBundle: String): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, _guildLocaleProvider, _userLocaleProvider)
    }

    override fun withPrefix(localizationPrefix: String?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, _guildLocaleProvider, _userLocaleProvider)
    }

    override fun switchBundle(localizationBundle: String): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, null, _guildLocaleProvider, _userLocaleProvider)
    }

    internal fun withLocales(guildLocale: Locale, userLocale: Locale): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, lazyOf(guildLocale), lazyOf(userLocale))
    }

    override fun localize(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): String {
        val localization = getLocalization(locale)
        val effectivePath = LocalizationUtils.getEffectivePath(localizationPrefix, localizationPath)
        val template = localization[effectivePath]
            ?: throwArgument("Found no localization template for '$effectivePath' (in bundle '$localizationBundle' with locale '${localization.effectiveLocale}')")

        return template.localize(*entries)
    }

    override fun localizeOrNull(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): String? {
        val localization = getLocalization(locale)
        val effectivePath = LocalizationUtils.getEffectivePath(localizationPrefix, localizationPath)
        val template = localization[effectivePath] ?: return null

        return template.localize(*entries)
    }

    private fun getLocalization(discordLocale: Locale) =
        localizationService.getInstance(localizationBundle, discordLocale)
            ?: throwInternal("Found no localization instance for bundle '$localizationBundle' and locale '$discordLocale', the root bundle should have been checked")

    override fun hasGuildLocale(): Boolean {
        return _guildLocaleProvider != null
    }

    private fun Locale.toProvider(): Lazy<Locale> = lazyOf(this)
}
