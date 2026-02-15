package dev.freya02.botcommands.typesafe.messages.internal

import dev.freya02.botcommands.typesafe.messages.internal.annotations.DynamicCall
import dev.freya02.botcommands.typesafe.messages.internal.exceptions.throwInternal
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

private typealias LocaleSupplier = () -> Locale

internal class MessageSourceContext(
    private val localizationService: LocalizationService,
    private val localizationBundle: String,
    guildLocaleSupplier: LocaleSupplier,
    userLocaleSupplier: LocaleSupplier?,
) {
    private val guildLocale: Locale by lazy(guildLocaleSupplier)
    private val userLocale: Locale? by if (userLocaleSupplier == null) lazyOf(null) else lazy(userLocaleSupplier)

    init {
        // At least the root bundle must exist
        requireNotNull(localizationService.getInstance(localizationBundle, Locale.ROOT)) {
            "A root localization bundle must exist for $localizationBundle"
        }
    }

    @DynamicCall
    fun localizeWith(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): String {
        val localization = localizationService.getInstance(localizationBundle, locale)
            ?: throwInternal("Found no localization instance for bundle '${localizationBundle}' and locale '${locale.toLanguageTag()}', the root bundle should have been checked")
        val template = localization[localizationPath]
            ?: throwInternal("Found no localization template for '$localizationPath' (in bundle '$localizationBundle' with locale '${localization.effectiveLocale}'), a root template should have been checked")

        return template.localize(*entries)
    }

    @DynamicCall
    fun localizeWith(locale: DiscordLocale, localizationPath: String, vararg entries: Localization.Entry): String {
        return localizeWith(locale.toLocale(), localizationPath, *entries)
    }

    @DynamicCall
    fun localizePreferringUser(localizationPath: String, vararg entries: Localization.Entry): String =
        localizeWith(userLocale ?: guildLocale, localizationPath, *entries)

    @DynamicCall
    fun localizeWithGuild(localizationPath: String, vararg entries: Localization.Entry): String =
        localizeWith(guildLocale, localizationPath, *entries)
}
