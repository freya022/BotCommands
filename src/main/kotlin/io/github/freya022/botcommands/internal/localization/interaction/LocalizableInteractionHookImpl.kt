package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteractionHook
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import java.util.*

internal class LocalizableInteractionHookImpl internal constructor(
    private val interactionHook: InteractionHook,
    private val localizableInteraction: LocalizableInteractionImpl,
) : LocalizableInteractionHook,
    InteractionHook by interactionHook {

    override fun sendUser(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message> =
        interactionHook.sendMessage(localizableInteraction.getUserMessage(localizationPath, *entries))

    override fun sendGuild(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageCreateAction<Message> =
        interactionHook.sendMessage(localizableInteraction.getGuildMessage(localizationPath, *entries))

    override fun sendLocalized(
        locale: Locale,
        localizationPath: String,
        vararg entries: Localization.Entry,
    ): WebhookMessageCreateAction<Message> {
        return interactionHook.sendMessage(localizableInteraction.getLocalizedMessage(locale, localizationPath, *entries))
    }

    override fun editUser(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message> =
        interactionHook.editOriginal(localizableInteraction.getUserMessage(localizationPath, *entries))

    override fun editGuild(localizationPath: String, vararg entries: Localization.Entry): WebhookMessageEditAction<Message> =
        interactionHook.editOriginal(localizableInteraction.getGuildMessage(localizationPath, *entries))

    override fun editLocalized(
        locale: Locale,
        localizationPath: String,
        vararg entries: Localization.Entry
    ): WebhookMessageEditAction<Message> {
        return interactionHook.editOriginal(localizableInteraction.getLocalizedMessage(locale, localizationPath, *entries))
    }
}