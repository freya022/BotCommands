package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteractionHook
import io.github.freya022.botcommands.api.localization.interaction.LocalizableReplyCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import java.util.*

internal class LocalizableReplyCallbackImpl internal constructor(
    private val replyCallback: IReplyCallback,
    private val interaction: LocalizableInteractionImpl
) : LocalizableReplyCallback {
    override fun getHook(): LocalizableInteractionHook {
        return interaction.getHook()
    }

    override fun replyUser(localizationPath: String, vararg entries: Localization.Entry): ReplyCallbackAction =
        replyCallback.reply(interaction.getUserMessage(localizationPath, *entries))

    override fun replyGuild(localizationPath: String, vararg entries: Localization.Entry): ReplyCallbackAction =
        replyCallback.reply(interaction.getGuildMessage(localizationPath, *entries))

    override fun replyLocalized(
        locale: Locale,
        localizationPath: String,
        vararg entries: Localization.Entry,
    ): ReplyCallbackAction {
        return replyCallback.reply(interaction.getLocalizedMessage(locale, localizationPath, *entries))
    }
}