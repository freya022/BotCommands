package com.freya02.botcommands.internal.localization

import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import com.freya02.botcommands.api.localization.context.TextLocalizationContext
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

internal class LocalizationContextImpl(
    private val localizationBundle: String,
    private val localizationPrefix: String?,
    private val guildLocale: DiscordLocale?,
    private val userLocale: DiscordLocale?
) : TextLocalizationContext, AppLocalizationContext {
    override fun withGuildLocale(guildLocale: DiscordLocale?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun withUserLocale(userLocale: DiscordLocale?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun withBundle(localizationBundle: String): LocalizationContextImpl {
        return LocalizationContextImpl(localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun withPrefix(localizationPrefix: String?): LocalizationContextImpl {
        return LocalizationContextImpl(localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    fun withLocales(guildLocale: DiscordLocale, userLocale: DiscordLocale): LocalizationContextImpl {
        return LocalizationContextImpl(localizationBundle, localizationPrefix, guildLocale, userLocale)
    }

    override fun localize(
        locale: DiscordLocale,
        localizationBundle: String,
        localizationPath: String,
        vararg entries: Localization.Entry
    ): String {
        val instance = Localization.getInstance(localizationBundle, Locale.forLanguageTag(locale.locale))
            ?: throwUser("Found no localization instance for bundle '$localizationBundle' and locale '$locale'")

        val effectivePath = when (localizationPrefix) {
            null -> localizationPath
            else -> "$localizationPrefix.$localizationPath"
        }

        val template = instance[effectivePath]
            ?: throwUser("Found no localization template for '$effectivePath' (in bundle '$localizationBundle' with locale '${instance.effectiveLocale}')")

        return template.localize(*entries)
    }

    override fun localize(localizationBundle: String, localizationPath: String, vararg entries: Localization.Entry): String = when {
        userLocale != null -> localizeUser(localizationPath, localizationBundle, *entries)
        guildLocale != null -> localizeGuild(localizationPath, localizationBundle, *entries)
        else -> localize(DiscordLocale.ENGLISH_US, localizationBundle, localizationPath, *entries)
    }

    override fun localize(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): String =
        localize(locale, localizationBundle, localizationPath, *entries)

    override fun localize(localizationPath: String, vararg entries: Localization.Entry): String = when {
        userLocale != null -> localizeUser(localizationPath, *entries)
        guildLocale != null -> localizeGuild(localizationPath, *entries)
        else -> localize(DiscordLocale.ENGLISH_US, localizationPath, *entries)
    }

    override fun getEffectiveLocale(): DiscordLocale = userLocale ?: guildLocale ?: DiscordLocale.ENGLISH_US

    override fun getLocalizationPrefix(): String? {
        return localizationPrefix
    }

    override fun getLocalizationBundle(): String {
        return localizationBundle
    }

    override fun hasGuildLocale(): Boolean {
        return guildLocale != null
    }

    override fun getGuildLocale(): DiscordLocale =
        guildLocale ?: throwUser("Cannot guild localize on an event which doesn't provide guild localization")

    override fun getUserLocale(): DiscordLocale =
        userLocale ?: throwUser("Cannot user localize on an event which doesn't provide user localization")
}