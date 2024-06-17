package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

internal class LocalizationContextImpl(
    private val localizationService: LocalizationService,
    override val localizationBundle: String,
    override val localizationPrefix: String?,
    private val _guildLocale: DiscordLocale?,
    private val _userLocale: DiscordLocale?
) : TextLocalizationContext, AppLocalizationContext {
    override val userLocale: DiscordLocale
        get() = _userLocale ?: throwArgument("Cannot user localize on an event which doesn't provide user localization")

    override val guildLocale: DiscordLocale
        get() = _guildLocale ?: throwArgument("Cannot guild localize on an event which doesn't provide guild localization")

    override val effectiveLocale: DiscordLocale
        get() = when {
            _userLocale != null -> _userLocale
            hasGuildLocale() -> guildLocale
            else -> DiscordLocale.ENGLISH_US
        }

    init {
        // At least the root bundle must exists
        requireNotNull(localizationService.getInstance(localizationBundle, Locale.ROOT)) {
            "A root localization bundle must exist for $localizationBundle"
        }
    }

    override fun withGuildLocale(guildLocale: DiscordLocale?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, guildLocale, _userLocale)
    }

    override fun withUserLocale(userLocale: DiscordLocale?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, _guildLocale, userLocale)
    }

    override fun withBundle(localizationBundle: String): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, _guildLocale, _userLocale)
    }

    override fun withPrefix(localizationPrefix: String?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, _guildLocale, _userLocale)
    }

    override fun switchBundle(localizationBundle: String): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, null, _guildLocale, _userLocale)
    }

    fun withLocales(guildLocale: DiscordLocale, userLocale: DiscordLocale): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun localize(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): String {
        val localization = getLocalization(locale)
        val effectivePath = getEffectivePath(localizationPath)
        val template = localization[effectivePath]
            ?: throwArgument("Found no localization template for '$effectivePath' (in bundle '$localizationBundle' with locale '${localization.effectiveLocale}')")

        return template.localize(*entries)
    }

    override fun localizeOrNull(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): String? {
        val localization = getLocalization(locale)
        val effectivePath = getEffectivePath(localizationPath)
        val template = localization[effectivePath] ?: return null

        return template.localize(*entries)
    }

    private fun getLocalization(discordLocale: DiscordLocale) =
        localizationService.getInstance(localizationBundle, discordLocale.toLocale())
            ?: throwInternal("Found no localization instance for bundle '$localizationBundle' and locale '$discordLocale', the root bundle should have been checked")

    private fun getEffectivePath(localizationPath: String) = when (localizationPrefix) {
        null -> localizationPath
        else -> "$localizationPrefix.$localizationPath"
    }

    override fun hasGuildLocale(): Boolean {
        return _guildLocale != null
    }
}