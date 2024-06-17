package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.LocalizableAction
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.internal.utils.throwArgument
import java.util.*

internal abstract class AbstractLocalizableAction(
    localizationConfig: BLocalizationConfig,
    protected val localizationService: LocalizationService,
) : LocalizableAction {
    private val responseBundles = localizationConfig.responseBundles

    override var localizationBundle: String? = null
    override var localizationPrefix: String? = null

    override fun getLocalizedMessage(locale: Locale, localizationPath: String, vararg entries: Localization.Entry): String {
        iterateBundles(locale, localizationPath) { localization, effectivePath ->
            val template = localization[effectivePath] ?: return@iterateBundles
            return template.localize(*entries)
        }
    }

    private inline fun iterateBundles(locale: Locale, localizationPath: String, block: (localization: Localization, effectivePath: String) -> Unit): Nothing {
        val effectivePath = getEffectivePath(localizationPath)
        localizationBundle?.let { localizationBundle ->
            val localization = localizationService.getInstance(localizationBundle, locale) ?:
            throwArgument("Could not find a bundle named '$localizationBundle'")

            // Can do a non-local return, skipping the exception below
            block(localization, effectivePath)

            throwArgument("Could not find a localization template at path '$effectivePath' in bundle '$localizationBundle'")
        }

        responseBundles.forEach { bundleName ->
            val localization = localizationService.getInstance(bundleName, locale) ?: return@forEach
            // Can do a non-local return, skipping the exception below
            block(localization, effectivePath)
        }

        throwArgument("Could not find a bundle with locale '$locale' and path '$effectivePath', registered bundles: $responseBundles")
    }

    private fun getEffectivePath(localizationPath: String) = when (localizationPrefix) {
        null -> localizationPath
        else -> "$localizationPrefix.$localizationPath"
    }
}