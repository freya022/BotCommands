package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.logger
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.utils.FileUpload
import java.io.InputStream
import java.util.function.Consumer
import javax.annotation.CheckReturnValue

private val logger = KotlinLogging.logger<BaseCommandEvent>()

internal open class BaseCommandEventImpl(
    context: BContext,
    event: MessageReceivedEvent,
    arguments: String,
    cancellableRateLimit: CancellableRateLimit
) : BaseCommandEvent(context, event.jda, event.responseNumber, event.message),
    CancellableRateLimit by cancellableRateLimit {

    private val argumentsStr: String = arguments

    override fun getArgumentsStrList(): List<String> = when {
        argumentsStr.isNotBlank() -> argumentsStr.split(' ').dropLastWhile { it.isEmpty() }
        else -> listOf()
    }

    override fun getArgumentsStr(): String = argumentsStr

    override fun reportError(message: String, e: Throwable) {
        channel.sendMessage(message).queue(null) { t: Throwable? -> logger.error(t) { "Could not send message to channel : $message" } }
        context.dispatchException(message, e)
    }

    override fun failureReporter(message: String): Consumer<in Throwable> {
        return Consumer { t: Throwable -> reportError(message, t) }
    }

    override fun getAuthorBestName(): String {
        return member.effectiveName
    }

    override fun getDefaultEmbed(): EmbedBuilder {
        return context.defaultEmbedSupplier.get()
    }

    override fun getDefaultIconStream(): InputStream? = context.defaultEmbedFooterIconSupplier.get()

    override fun sendWithEmbedFooterIcon(embed: MessageEmbed, onException: Consumer<in Throwable>): RestAction<Message> =
        sendWithEmbedFooterIcon(channel, embed, onException)

    @CheckReturnValue
    override fun sendWithEmbedFooterIcon(
        channel: MessageChannel,
        embed: MessageEmbed,
        onException: Consumer<in Throwable>
    ): RestAction<Message> = sendWithEmbedFooterIcon(channel, defaultIconStream, embed, onException)

    @CheckReturnValue
    override fun sendWithEmbedFooterIcon(
        channel: MessageChannel,
        iconStream: InputStream?,
        embed: MessageEmbed,
        onException: Consumer<in Throwable>
    ): RestAction<Message> = when {
        iconStream != null -> channel.sendTyping().flatMap { channel.sendFiles(FileUpload.fromData(iconStream, "icon.jpg")).setEmbeds(embed) }
        else -> channel.sendTyping().flatMap { channel.sendMessageEmbeds(embed) }
    }

    @CheckReturnValue
    override fun reactSuccess(): RestAction<Void> = channel.addReactionById(messageId, SUCCESS)

    @CheckReturnValue
    override fun reactError(): RestAction<Void> = channel.addReactionById(messageId, ERROR)

    override fun respond(text: CharSequence): MessageCreateAction = channel.sendMessage(text)

    override fun respondFormat(format: String, vararg args: Any): MessageCreateAction = channel.sendMessageFormat(format, *args)

    override fun respond(embed: MessageEmbed, vararg other: MessageEmbed): MessageCreateAction = channel.sendMessageEmbeds(embed, *other)

    override fun respondFile(vararg fileUploads: FileUpload): MessageCreateAction = channel.sendFiles(*fileUploads)

    @CheckReturnValue
    override fun reply(text: CharSequence): MessageCreateAction = message.reply(text)

    @CheckReturnValue
    override fun replyFormat(format: String, vararg args: Any): MessageCreateAction = message.replyFormat(format, *args)

    @CheckReturnValue
    override fun reply(embed: MessageEmbed, vararg other: MessageEmbed): MessageCreateAction = message.replyEmbeds(embed, *other)

    @CheckReturnValue
    override fun replyFile(vararg fileUploads: FileUpload): RestAction<Message> =
        channel.sendTyping().flatMap { message.replyFiles(*fileUploads) }

    override fun indicateError(text: CharSequence): RestAction<Message> = when {
        guild.selfMember.hasPermission(guildChannel, Permission.MESSAGE_ADD_REACTION) -> reactError().flatMap { channel.sendMessage(text) }
        else -> channel.sendMessage(text)
    }

    override fun indicateErrorFormat(format: String, vararg args: Any): RestAction<Message> = when {
        guild.selfMember.hasPermission(guildChannel, Permission.MESSAGE_ADD_REACTION) -> reactError().flatMap { channel.sendMessageFormat(format, *args) }
        else -> channel.sendMessageFormat(format, *args)
    }

    override fun indicateError(embed: MessageEmbed, vararg other: MessageEmbed): RestAction<Message> = when {
        guild.selfMember.hasPermission(guildChannel, Permission.MESSAGE_ADD_REACTION) -> reactError().flatMap { channel.sendMessageEmbeds(embed, *other) }
        else -> channel.sendMessageEmbeds(embed, *other)
    }

    companion object {
        @JvmField val SUCCESS = EmojiUtils.resolveJDAEmoji(":white_check_mark:")
        @JvmField val ERROR = EmojiUtils.resolveJDAEmoji(":x:")
    }
}