package dev.freya02.botcommands.jda.ktx.messages

import dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.utils.AttachedFile

/**
 * Same as [IMessageEditCallback.editMessage].
 *
 * @param content    Content to override existing content, `null` to remove the content
 * @param embeds     Embeds to override existing embeds, `emptyList()` to remove all
 * @param components Components to override existing components, `emptyList()` to remove all
 * @param files      Files to override existing files, `emptyList()` to remove all
 * @param replace    `true` to replace the entire message, `false` to only replace specified parts
 * @param block      Additional configuration, can override the previously provided parameters
 *
 * @see IMessageEditCallback.editMessage
 */
@ReplaceJdaKtx
@Suppress("FunctionName") // To facilitate imports
inline fun IMessageEditCallback.editMessage_(
    content: String? = null,
    embeds: Collection<MessageEmbed>? = null,
    components: Collection<MessageTopLevelComponent>? = null,
    files: Collection<AttachedFile>? = null,
    replace: Boolean = false,
    block: InlineMessageEdit.() -> Unit = {},
): MessageEditCallbackAction = editMessage(MessageEdit(content, embeds, components, files, mentions = null, replace, block))

/**
 * Same as [InteractionHook.editMessageById].
 *
 * @param id         ID of the *followup* message to edit, `@original` is equivalent to [InteractionHook.editOriginal]
 * @param content    Content to override existing content, `null` to remove the content
 * @param embeds     Embeds to override existing embeds, `emptyList()` to remove all
 * @param components Components to override existing components, `emptyList()` to remove all
 * @param files      Files to override existing files, `emptyList()` to remove all
 * @param replace    `true` to replace the entire message, `false` to only replace specified parts
 * @param block      Additional configuration, can override the previously provided parameters
 *
 * @see InteractionHook.editMessageById
 */
@ReplaceJdaKtx
inline fun InteractionHook.editMessage(
    id: String = "@original",
    content: String? = null,
    embeds: Collection<MessageEmbed>? = null,
    components: Collection<MessageTopLevelComponent>? = null,
    files: Collection<AttachedFile>? = null,
    replace: Boolean = false,
    block: InlineMessageEdit.() -> Unit = {},
): WebhookMessageEditAction<Message> = editMessageById(id, MessageEdit(content, embeds, components, files, mentions = null, replace, block))

/**
 * Same as [MessageChannel.editMessageById].
 *
 * @param id         ID of the message to edit
 * @param content    Content to override existing content, `null` to remove the content
 * @param embeds     Embeds to override existing embeds, `emptyList()` to remove all
 * @param components Components to override existing components, `emptyList()` to remove all
 * @param files      Files to override existing files, `emptyList()` to remove all
 * @param replace    `true` to replace the entire message, `false` to only replace specified parts
 * @param block      Additional configuration, can override the previously provided parameters
 *
 * @see MessageChannel.editMessageById
 */
@ReplaceJdaKtx
inline fun MessageChannel.editMessage(
    id: String,
    content: String? = null,
    embeds: Collection<MessageEmbed>? = null,
    components: Collection<MessageTopLevelComponent>? = null,
    files: Collection<AttachedFile>? = null,
    replace: Boolean = false,
    block: InlineMessageEdit.() -> Unit = {},
): MessageEditAction = editMessageById(id, MessageEdit(content, embeds, components, files, mentions = null, replace, block))

/**
 * Same as [Message.editMessage].
 *
 * @param content    Content to override existing content, `null` to remove the content
 * @param embeds     Embeds to override existing embeds, `emptyList()` to remove all
 * @param components Components to override existing components, `emptyList()` to remove all
 * @param files      Files to override existing files, `emptyList()` to remove all
 * @param replace    `true` to replace the entire message, `false` to only replace specified parts
 * @param block      Additional configuration, can override the previously provided parameters
 *
 * @see Message.editMessage
 */
@ReplaceJdaKtx
inline fun Message.edit(
    content: String? = null,
    embeds: Collection<MessageEmbed>? = null,
    components: Collection<MessageTopLevelComponent>? = null,
    files: Collection<AttachedFile>? = null,
    replace: Boolean = false,
    block: InlineMessageEdit.() -> Unit = {},
): MessageEditAction = editMessage(MessageEdit(content, embeds, components, files, mentions = null, replace, block))
