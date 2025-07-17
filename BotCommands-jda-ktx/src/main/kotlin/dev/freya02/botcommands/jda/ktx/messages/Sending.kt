package dev.freya02.botcommands.jda.ktx.messages

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

/**
 * @see IReplyCallback.reply
 */
inline fun IReplyCallback.reply_(
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    ephemeral: Boolean = false,
    builder: InlineMessageCreate.() -> Unit,
): ReplyCallbackAction = reply(MessageCreate(tts, mentions, builder)).setEphemeral(ephemeral)

/**
 * @see InteractionHook.sendMessage
 */
inline fun InteractionHook.send(
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    ephemeral: Boolean = false,
    builder: InlineMessageCreate.() -> Unit,
): WebhookMessageCreateAction<Message> = sendMessage(MessageCreate(tts, mentions, builder)).setEphemeral(ephemeral)

/**
 * @see MessageChannel.sendMessage
 */
inline fun MessageChannel.send(
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    builder: InlineMessageCreate.() -> Unit,
): MessageCreateAction = sendMessage(MessageCreate(tts, mentions, builder))

/**
 * @see MessageChannel.sendMessage
 * @see MessageCreateAction.setMessageReference
 */
inline fun Message.reply_(
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    builder: InlineMessageCreate.() -> Unit,
): MessageCreateAction = channel.sendMessage(MessageCreate(tts, mentions, builder)).setMessageReference(this)
