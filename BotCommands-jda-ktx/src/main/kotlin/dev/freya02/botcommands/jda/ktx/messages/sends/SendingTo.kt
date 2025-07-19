package dev.freya02.botcommands.jda.ktx.messages.sends

import dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData

/**
 * @see IReplyCallback.reply
 */
@DeprecatedInBcCore
fun MessageCreateData.send(callback: IReplyCallback, ephemeral: Boolean = false): ReplyCallbackAction =
    callback.reply(this).setEphemeral(ephemeral)

/**
 * @see InteractionHook.sendMessage
 */
@DeprecatedInBcCore
fun MessageCreateData.send(hook: InteractionHook, ephemeral: Boolean = false): WebhookMessageCreateAction<Message> =
    hook.sendMessage(this).setEphemeral(ephemeral)

/**
 * @see MessageChannel.sendMessage
 */
@DeprecatedInBcCore
fun MessageCreateData.send(channel: MessageChannel): MessageCreateAction =
    channel.sendMessage(this)
