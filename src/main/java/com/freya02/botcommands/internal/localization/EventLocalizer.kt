package com.freya02.botcommands.internal.localization

import com.freya02.botcommands.annotations.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.GuildLocalizable
import com.freya02.botcommands.api.localization.Localizable
import com.freya02.botcommands.api.localization.Localization
import com.freya02.botcommands.api.localization.UserLocalizable
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*
import kotlin.reflect.KFunction

internal class EventLocalizer(
    private val context: BContextImpl,
    private val function: KFunction<*>?,
    private val guildLocale: DiscordLocale?,
    private val userLocale: DiscordLocale?
) : UserLocalizable, GuildLocalizable, Localizable {
    override fun localize(
        locale: DiscordLocale,
        localizationBundle: String,
        localizationPath: String,
        vararg entries: Localization.Entry
    ): String {
        val localizationManager = context.localizationManager
        val instance = Localization.getInstance(localizationBundle, Locale.forLanguageTag(locale.locale))
            ?: throwUser("Found no localization instance for bundle '$localizationBundle' and locale '$locale'")

        val effectivePath = when {
            function != null -> when (val localizationPrefix = localizationManager.getLocalizationPrefix(function)) {
                null -> localizationPath
                else -> "$localizationPrefix.$localizationPath"
            }
            else -> localizationPath
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

    override fun getLocalizationBundle(): String {
        checkNotNull(function) { "Cannot use predefined localization bundles in this event" }

        return context.localizationManager.getLocalizationBundle(function)
            ?: throwUser(function, "You cannot use this localization method without having the command, or the class which contains it, be annotated with @" + LocalizationBundle::class.simpleName)
    }

    override fun getGuildLocale(): DiscordLocale =
        guildLocale ?: throwUser("Cannot guild localize on an event which doesn't provide guild localization")

    override fun getUserLocale(): DiscordLocale =
        userLocale ?: throwUser("Cannot guild localize on an event which doesn't provide guild localization")
}