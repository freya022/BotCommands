package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.utils.EmojiUtils;
import com.freya02.botcommands.internal.BContextImpl;
import kotlin.reflect.KFunction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.CheckReturnValue;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BaseCommandEventImpl extends BaseCommandEvent {
	public static final Emoji SUCCESS = EmojiUtils.resolveJDAEmoji(":white_check_mark:");
	public static final Emoji ERROR = EmojiUtils.resolveJDAEmoji(":x:");
	private static final Logger LOGGER = Logging.getLogger();

	protected final BContext context;
	protected final String argumentsStr;

	protected final GuildMessageChannel channel;

	public BaseCommandEventImpl(@NotNull BContextImpl context, @Nullable KFunction<?> function, MessageReceivedEvent event, String arguments) {
		super(context, function, event.getJDA(), event.getResponseNumber(), event.getMessage());

		this.context = context;
		this.argumentsStr = arguments;
		this.channel = event.getGuildChannel();
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

		context.dispatchException(message, e);
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
	@NotNull
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

	@NotNull
	@Override
	public RestAction<Message> respond(@NotNull CharSequence text) {
		return channel.sendMessage(text);
	}

	@NotNull
	@Override
	public RestAction<Message> respondFormat(@NotNull String format, @NotNull Object... args) {
		return channel.sendMessageFormat(format, args);
	}

	@NotNull
	@Override
	public RestAction<Message> respond(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other) {
		return channel.sendMessageEmbeds(embed, other);
	}

	@NotNull
	@Override
	public RestAction<Message> respondFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options) {
		return channel.sendFile(data, fileName, options);
	}

	@NotNull
	@Override
	public RestAction<Message> respondFile(@NotNull byte[] data, @NotNull String fileName, @NotNull AttachmentOption... options) {
		return channel.sendFile(data, fileName, options);
	}

	@Override
	@CheckReturnValue
	@NotNull
	public RestAction<Message> reply(@NotNull CharSequence text) {
		return getMessage().reply(text);
	}

	@Override
	@CheckReturnValue
	@NotNull
	public RestAction<Message> replyFormat(@NotNull String format, @NotNull Object... args) {
		return getMessage().replyFormat(format, args);
	}

	@Override
	@CheckReturnValue
	@NotNull
	public RestAction<Message> reply(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other) {
		return getMessage().replyEmbeds(embed, other);
	}

	@Override
	@CheckReturnValue
	@NotNull
	public RestAction<Message> replyFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options) {
		return channel.sendTyping().flatMap(v -> getMessage().reply(data, fileName, options));
	}

	@Override
	@CheckReturnValue
	@NotNull
	public RestAction<Message> replyFile(@NotNull byte[] data, @NotNull String fileName, @NotNull AttachmentOption... options) {
		return channel.sendTyping().flatMap(v -> getMessage().reply(data, fileName, options));
	}

	@NotNull
	@Override
	public RestAction<Message> indicateError(@NotNull CharSequence text) {
		if (getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
			return reactError().flatMap(v -> channel.sendMessage(text));
		} else {
			return channel.sendMessage(text);
		}
	}

	@NotNull
	@Override
	public RestAction<Message> indicateErrorFormat(@NotNull String format, @NotNull Object... args) {
		if (getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
			return reactError().flatMap(v -> channel.sendMessageFormat(format, args));
		} else {
			return channel.sendMessageFormat(format, args);
		}
	}

	@NotNull
	@Override
	public RestAction<Message> indicateError(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other) {
		if (getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
			return reactError().flatMap(v -> channel.sendMessageEmbeds(embed, other));
		} else {
			return channel.sendMessageEmbeds(embed, other);
		}
	}
}
