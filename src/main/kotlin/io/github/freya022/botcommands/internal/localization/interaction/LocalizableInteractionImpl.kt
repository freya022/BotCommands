package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.localization.DefaultMessages
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
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
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
    private val defaultMessagesFactory: DefaultMessagesFactory,
) : AbstractLocalizableAction(localizationConfig, localizationService),
    LocalizableInteraction {

    private val userLocale: Locale by lazy { userLocaleProvider.getLocale(deferrableCallback) }
    private val guildLocale: Locale by lazy { guildLocaleProvider.getLocale(deferrableCallback) }

    override fun getLocalizationContext(bundleName: String, pathPrefix: String?): AppLocalizationContext {
        return LocalizationContext.create(
            localizationService,
            bundleName,
            pathPrefix,
            guildLocale = guildLocaleProvider.getDiscordLocale(deferrableCallback),
            userLocale = userLocaleProvider.getDiscordLocale(deferrableCallback)
        )
    }

    override fun getDefaultMessages(): DefaultMessages {
        return defaultMessagesFactory.get(deferrableCallback)
    }

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