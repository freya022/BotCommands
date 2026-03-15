package dev.freya02.botcommands.jda.ktx.messages

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.utils.messages.MessageEditData

/**
 * @see IMessageEditCallback.editMessage
 */
fun MessageEditData.edit(callback: IMessageEditCallback): MessageEditCallbackAction =
    callback.editMessage(this)

/**
 * @see InteractionHook.editOriginal
 */
fun MessageEditData.edit(hook: InteractionHook): WebhookMessageEditAction<Message> =
    hook.editOriginal(this)

/**
 * @see MessageChannel.editMessageById
 */
fun MessageEditData.edit(channel: MessageChannel, id: Long): MessageEditAction =
    channel.editMessageById(id, this)
