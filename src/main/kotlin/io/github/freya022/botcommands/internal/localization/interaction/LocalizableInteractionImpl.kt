package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteraction
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteractionHook
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import java.util.*

internal class LocalizableInteractionImpl internal constructor(
    private val deferrableCallback: IDeferrableCallback,
    private val localizationService: LocalizationService,
    localizationConfig: BLocalizationConfig,
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
) : LocalizableInteraction {
    private val responseBundles = localizationConfig.responseBundles

    override var localizationBundle: String? = null
    override var localizationPrefix: String? = null

    private val _userLocale: DiscordLocale by lazy { userLocaleProvider.getLocale(deferrableCallback) }
    private val _guildLocale: DiscordLocale by lazy { guildLocaleProvider.getLocale(deferrableCallback) }

    override fun getUserLocale(): DiscordLocale = _userLocale
    override fun getGuildLocale(): DiscordLocale = _guildLocale

    override fun getHook(): LocalizableInteractionHook {
        return LocalizableInteractionHookImpl(deferrableCallback.hook, this)
    }

    override fun getLocalizationContext(bundleName: String, localizationPrefix: String?): AppLocalizationContext =
        LocalizationContext.create(
            localizationService,
            bundleName,
            localizationPrefix,
            if (deferrableCallback.isFromGuild) getGuildLocale() else null,
            getUserLocale()
        )

    internal fun getLocalizedTemplate(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): String {
        iterateBundles(locale, localizationPath) { localization, effectivePath ->
            val template = localization[effectivePath] ?: return@iterateBundles
            return template.localize(*entries)
        }
    }

    private inline fun iterateBundles(locale: Locale, localizationPath: String, block: (localization: Localization, effectivePath: String) -> Unit): Nothing {
        val effectivePath = getEffectivePath(localizationPath)
        localizationBundle?.let { localizationBundle ->
            val localization = localizationService.getInstance(localizationBundle, locale)
            if (localization != null)
                // Can do a non-local return, skipping the exception below
                block(localization, effectivePath)

            throw IllegalArgumentException("Could not find a bundle named '$localizationBundle' with locale '$locale', registered bundles: $responseBundles")
        }

        responseBundles.forEach { bundleName ->
            val localization = localizationService.getInstance(bundleName, locale) ?: return@forEach
            // Can do a non-local return, skipping the exception below
            block(localization, effectivePath)
        }

        throw IllegalArgumentException("Could not find a bundle with locale '$locale' and path '$effectivePath', registered bundles: $responseBundles")
    }

    private fun getEffectivePath(localizationPath: String) = when (localizationPrefix) {
        null -> localizationPath
        else -> "$localizationPrefix.$localizationPath"
    }
}