package io.github.freya022.botcommands.api.localization.interaction

import io.github.freya022.botcommands.api.localization.LocalizableAction
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.mapToEntries

/**
 * Allows you to configure the localization settings of this interaction event,
 * retrieve a [LocalizableInteractionHook],
 * as well as retrieve a localization context from it.
 *
 * Always combined with [LocalizableReplyCallback] and/or [LocalizableEditCallback].
 *
 * ### Configuring localization bundle and prefix
 * You can change the bundle and prefix in the first lines of your interaction handler,
 * with [localizationBundle] and [localizationPrefix].
 *
 * ### Configuring the user / guild locale source
 * They are by default retrieved from the interaction,
 * but you can get them in other ways by implementing [UserLocaleProvider] and/or [GuildLocaleProvider].
 *
 * @see LocalizableReplyCallback
 * @see LocalizableEditCallback
 * @see LocalizableInteractionHook
 */
interface LocalizableInteraction : LocalizableAction {
    fun getHook(): LocalizableInteractionHook

    fun getUserMessage(localizationPath: String, vararg entries: Localization.Entry): String

    fun getGuildMessage(localizationPath: String, vararg entries: Localization.Entry): String
}

fun LocalizableInteraction.getUserMessage(localizationPath: String, vararg entries: PairEntry): String =
    getUserMessage(localizationPath, *entries.mapToEntries())

fun LocalizableInteraction.getGuildMessage(localizationPath: String, vararg entries: PairEntry): String =
    getGuildMessage(localizationPath, *entries.mapToEntries())