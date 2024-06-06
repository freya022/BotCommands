package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteraction
import io.github.freya022.botcommands.api.localization.interaction.LocalizableReplyCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import java.util.*

internal class LocalizableReplyCallbackImpl internal constructor(
    private val replyCallback: IReplyCallback,
    interaction: LocalizableInteractionImpl
) : LocalizableReplyCallback,
    LocalizableInteraction by interaction {

    override fun replyUser(localizationPath: String, vararg entries: Localization.Entry): ReplyCallbackAction =
        replyCallback.reply(getUserMessage(localizationPath, *entries))

    override fun replyGuild(localizationPath: String, vararg entries: Localization.Entry): ReplyCallbackAction =
        replyCallback.reply(getGuildMessage(localizationPath, *entries))

    override fun replyLocalized(
        locale: Locale,
        localizationPath: String,
        vararg entries: Localization.Entry,
    ): ReplyCallbackAction {
        return replyCallback.reply(getLocalizedMessage(locale, localizationPath, *entries))
    }
}