package dev.freya02.botcommands.jda.ktx.messages

import dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData

/**
 * @see InteractionHook.editOriginal
 */
@DeprecatedInBcCore
fun InteractionHook.replaceWith(data: MessageEditData): WebhookMessageEditAction<Message> =
    editOriginal(data).setReplace(true)

/**
 * @see InteractionHook.editOriginal
 */
@DeprecatedInBcCore
fun InteractionHook.replaceWith(data: MessageCreateData): WebhookMessageEditAction<Message> =
    editOriginal(data.toEditData()).setReplace(true)

/**
 * @see InteractionHook.editOriginal
 */
@DeprecatedInBcCore
fun InteractionHook.replaceWith(content: String): WebhookMessageEditAction<Message> =
    editOriginal(content).setReplace(true)
