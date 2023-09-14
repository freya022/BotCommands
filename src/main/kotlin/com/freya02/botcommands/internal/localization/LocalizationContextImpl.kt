package com.freya02.botcommands.internal.localization

import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.LocalizationService
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import com.freya02.botcommands.api.localization.context.TextLocalizationContext
import com.freya02.botcommands.internal.utils.throwInternal
import com.freya02.botcommands.internal.utils.throwUser
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
        get() = _userLocale ?: throwUser("Cannot user localize on an event which doesn't provide user localization")

    override val guildLocale: DiscordLocale
        get() = _guildLocale ?: throwUser("Cannot guild localize on an event which doesn't provide guild localization")

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
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun withUserLocale(userLocale: DiscordLocale?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun withBundle(localizationBundle: String): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun withPrefix(localizationPrefix: String?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    fun withLocales(guildLocale: DiscordLocale, userLocale: DiscordLocale): LocalizationContextImpl {
        return LocalizationContextImpl(localizationService, localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun localize(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): String {
        val instance = localizationService.getInstance(localizationBundle, Locale.forLanguageTag(locale.locale))
            ?: throwInternal("Found no localization instance for bundle '$localizationBundle' and locale '$locale', the root bundle should have been checked")

        val effectivePath = when (localizationPrefix) {
            null -> localizationPath
            else -> "$localizationPrefix.$localizationPath"
        }

        val template = instance[effectivePath]
            ?: throwUser("Found no localization template for '$effectivePath' (in bundle '$localizationBundle' with locale '${instance.effectiveLocale}')")

        return template.localize(*entries)
    }

    override fun hasGuildLocale(): Boolean {
        return _guildLocale != null
    }
}