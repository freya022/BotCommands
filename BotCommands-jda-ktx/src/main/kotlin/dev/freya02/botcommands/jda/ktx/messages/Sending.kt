package dev.freya02.botcommands.jda.ktx.messages

import dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageRequest

/**
 * @see IReplyCallback.reply
 */
@ReplaceJdaKtx
inline fun IReplyCallback.reply_(
    content: String? = null,
    embeds: Collection<MessageEmbed> = NO_CONTENT,
    files: Collection<FileUpload> = NO_CONTENT,
    components: Collection<MessageTopLevelComponent> = NO_CONTENT,
    useComponentsV2: Boolean = MessageRequest.isDefaultUseComponentsV2(),
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    ephemeral: Boolean = false,
    builder: InlineMessageCreate.() -> Unit = {},
): ReplyCallbackAction = reply(MessageCreate(content, embeds, files, components, useComponentsV2, tts, mentions, builder)).setEphemeral(ephemeral)

/**
 * @see InteractionHook.sendMessage
 */
@ReplaceJdaKtx
inline fun InteractionHook.send(
    content: String? = null,
    embeds: Collection<MessageEmbed> = NO_CONTENT,
    files: Collection<FileUpload> = NO_CONTENT,
    components: Collection<MessageTopLevelComponent> = NO_CONTENT,
    useComponentsV2: Boolean = MessageRequest.isDefaultUseComponentsV2(),
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    ephemeral: Boolean = false,
    builder: InlineMessageCreate.() -> Unit = {},
): WebhookMessageCreateAction<Message> = sendMessage(MessageCreate(content, embeds, files, components, useComponentsV2, tts, mentions, builder)).setEphemeral(ephemeral)

/**
 * @see MessageChannel.sendMessage
 */
@ReplaceJdaKtx
inline fun MessageChannel.send(
    content: String? = null,
    embeds: Collection<MessageEmbed> = NO_CONTENT,
    files: Collection<FileUpload> = NO_CONTENT,
    components: Collection<MessageTopLevelComponent> = NO_CONTENT,
    useComponentsV2: Boolean = MessageRequest.isDefaultUseComponentsV2(),
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    builder: InlineMessageCreate.() -> Unit = {},
): MessageCreateAction = sendMessage(MessageCreate(content, embeds, files, components, useComponentsV2, tts, mentions, builder))

/**
 * @see MessageChannel.sendMessage
 * @see MessageCreateAction.setMessageReference
 */
@ReplaceJdaKtx
inline fun Message.reply_(
    content: String? = null,
    embeds: Collection<MessageEmbed> = NO_CONTENT,
    files: Collection<FileUpload> = NO_CONTENT,
    components: Collection<MessageTopLevelComponent> = NO_CONTENT,
    useComponentsV2: Boolean = MessageRequest.isDefaultUseComponentsV2(),
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    builder: InlineMessageCreate.() -> Unit = {},
): MessageCreateAction = channel.sendMessage(MessageCreate(content, embeds, files, components, useComponentsV2, tts, mentions, builder)).setMessageReference(this)
