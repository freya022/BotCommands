package com.freya02.botcommands.prefixed.impl;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.utils.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.slf4j.Logger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BaseCommandEventImpl extends BaseCommandEvent {
	public static final String SUCCESS = EmojiUtils.resolveEmojis(":white_check_mark:");
	public static final String ERROR = EmojiUtils.resolveEmojis(":x:");
	private static final Logger LOGGER = Logging.getLogger();

	protected final BContext context;
	protected final String argumentsStr;

	public BaseCommandEventImpl(BContext context, GuildMessageReceivedEvent event, String arguments) {
		super(event.getJDA(), event.getResponseNumber(), event.getMessage());

		this.context = context;
		this.argumentsStr = arguments;
	}

	@Override
	public BContext getContext() {
		return context;
	}

	@Override
	public List<String> getArgumentsStrList() {
		if (!argumentsStr.isBlank()) {
			return Arrays.asList(argumentsStr.split(" "));
		} else {
			return List.of();
		}
	}

	@Override
	public String getArgumentsStr() {
		return argumentsStr;
	}

	@Override
	public void reportError(String message, Throwable e) {
		channel.sendMessage(message).queue(null, t -> LOGGER.error("Could not send message to channel : {}", message));

		((BContextImpl) context).dispatchException(message, e);
	}

	@Override
	public Consumer<? super Throwable> failureReporter(String message) {
		return t -> reportError(message, t);
	}

	@Override
	public String getAuthorBestName() {
		return getMember().getEffectiveName();
	}

	@Override
	@Nonnull
	public EmbedBuilder getDefaultEmbed() {
		return context.getDefaultEmbedSupplier().get();
	}

	@Override
	@Nullable
	public InputStream getDefaultIconStream() {
		return context.getDefaultFooterIconSupplier().get();
	}

	@Override
	public RestAction<Message> sendWithEmbedFooterIcon(MessageEmbed embed, Consumer<? super Throwable> onException) {
		return sendWithEmbedFooterIcon(channel, embed, onException);
	}

	@Override
	@CheckReturnValue
	public RestAction<Message> sendWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed, Consumer<? super Throwable> onException) {
		return sendWithEmbedFooterIcon(channel, getDefaultIconStream(), embed, onException);
	}

	@Override
	@CheckReturnValue
	public RestAction<Message> sendWithEmbedFooterIcon(MessageChannel channel, InputStream iconStream, MessageEmbed embed, Consumer<? super Throwable> onException) {
		if (iconStream != null) {
			return channel.sendTyping().flatMap(v -> channel.sendFile(iconStream, "icon.jpg").setEmbeds(embed));
		} else {
			return channel.sendTyping().flatMap(v -> channel.sendMessageEmbeds(embed));
		}
	}

	@Override
	@CheckReturnValue
	public RestAction<Void> reactSuccess() {
		return channel.addReactionById(messageId, SUCCESS);
	}

	@Override
	@CheckReturnValue
	public RestAction<Void> reactError() {
		return channel.addReactionById(messageId, ERROR);
	}

	@Nonnull
	@Override
	public RestAction<Message> respond(@Nonnull CharSequence text) {
		return channel.sendMessage(text);
	}

	@Nonnull
	@Override
	public RestAction<Message> respondFormat(@Nonnull String format, @Nonnull Object... args) {
		return channel.sendMessageFormat(format, args);
	}

	@Nonnull
	@Override
	public RestAction<Message> respond(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other) {
		return channel.sendMessageEmbeds(embed, other);
	}

	@Nonnull
	@Override
	public RestAction<Message> respondFile(@Nonnull InputStream data, @Nonnull String fileName, @Nonnull AttachmentOption... options) {
		return channel.sendFile(data, fileName, options);
	}

	@Nonnull
	@Override
	public RestAction<Message> respondFile(@Nonnull byte[] data, @Nonnull String fileName, @Nonnull AttachmentOption... options) {
		return channel.sendFile(data, fileName, options);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> reply(@Nonnull CharSequence text) {
		return getMessage().reply(text);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> replyFormat(@Nonnull String format, @Nonnull Object... args) {
		return getMessage().replyFormat(format, args);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> reply(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other) {
		return getMessage().replyEmbeds(embed, other);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> replyFile(@Nonnull InputStream data, @Nonnull String fileName, @Nonnull AttachmentOption... options) {
		return channel.sendTyping().flatMap(v -> getMessage().reply(data, fileName, options));
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> replyFile(byte[] data, @Nonnull String fileName, @Nonnull AttachmentOption... options) {
		return channel.sendTyping().flatMap(v -> getMessage().reply(data, fileName, options));
	}

	@Nonnull
	@Override
	public RestAction<Message> indicateError(@Nonnull CharSequence text) {
		if (getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
			return reactError().flatMap(v -> channel.sendMessage(text));
		} else {
			return channel.sendMessage(text);
		}
	}

	@Nonnull
	@Override
	public RestAction<Message> indicateErrorFormat(@Nonnull String format, @Nonnull Object... args) {
		if (getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
			return reactError().flatMap(v -> channel.sendMessageFormat(format, args));
		} else {
			return channel.sendMessageFormat(format, args);
		}
	}

	@Nonnull
	@Override
	public RestAction<Message> indicateError(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other) {
		if (getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
			return reactError().flatMap(v -> channel.sendMessageEmbeds(embed, other));
		} else {
			return channel.sendMessageEmbeds(embed, other);
		}
	}
}
