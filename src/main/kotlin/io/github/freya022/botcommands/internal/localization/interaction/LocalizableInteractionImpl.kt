package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteraction
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteractionHook
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.internal.localization.AbstractLocalizableAction
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import java.util.*

internal class LocalizableInteractionImpl internal constructor(
    private val deferrableCallback: IDeferrableCallback,
    localizationService: LocalizationService,
    localizationConfig: BLocalizationConfig,
    userLocaleProvider: UserLocaleProvider,
    guildLocaleProvider: GuildLocaleProvider,
) : AbstractLocalizableAction(localizationConfig, localizationService),
    LocalizableInteraction,
    IDeferrableCallback by deferrableCallback {

    private val userLocale: Locale by lazy { userLocaleProvider.getLocale(deferrableCallback) }
    private val guildLocale: Locale by lazy { guildLocaleProvider.getLocale(deferrableCallback) }

    override fun getUserMessage(localizationPath: String, vararg entries: Localization.Entry): String {
        return getLocalizedMessage(userLocale, localizationPath, *entries)
    }

    override fun getGuildMessage(localizationPath: String, vararg entries: Localization.Entry): String {
        return getLocalizedMessage(guildLocale, localizationPath, *entries)
    }

    override fun getHook(): LocalizableInteractionHook {
        return LocalizableInteractionHookImpl(deferrableCallback.hook, this)
    }
}