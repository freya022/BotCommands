package com.freya02.botcommands;

import com.freya02.botcommands.utils.EmojiResolver;
import com.freya02.botcommands.utils.SimpleStream;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * <p>Base class containing several utility methods:</p>
 * <ul>
 *     <li>Unhandled exception reporting</li>
 *     <li>Send embeds with footer icon with specified icons / default icon</li>
 *     <li>Sending the command's help embed</li>
 *     <li>Adding a reaction to indicate command success/failure</li>
 * </ul>
 */
public class BaseCommandEvent extends GuildMessageReceivedEvent implements IBaseCommandEvent {
	private static final String SUCCESS = EmojiResolver.resolveEmojis(":white_check_mark:");
	private static final String ERROR = EmojiResolver.resolveEmojis(":x:");

	protected final CommandListener commandListener;
	protected final String commandName;
	protected final String argumentsStr;

	BaseCommandEvent(CommandListener commandListener, GuildMessageReceivedEvent event, String commandName, String arguments) {
		super(event.getJDA(), event.getResponseNumber(), event.getMessage());
		this.commandListener = commandListener;
		this.commandName = commandName;
		this.argumentsStr = arguments;
	}

	@Override
	public void showHelp() {
		final CommandInfo helpInfo = getCommandInfo("help");
		if (helpInfo == null) {
			System.err.println("ERROR: help command info not found");
			return;
		}

		((HelpCommand) helpInfo.getCommand()).getCommandHelp(this, commandName);
	}

	@Override
	public List<Long> getOwnerIds() {
		return commandListener.getOwnerIds();
	}

	@Override
	@Nullable
	public CommandInfo getCommandInfo(String cmdName) {
		return commandListener.getCommandInfo(cmdName);
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
		channel.sendMessage(message).queue(null, t -> System.err.println("Could not send message to channel : " + message));

		final User owner = getJDA().getUserById(commandListener.getOwnerIds().get(0));

		if (owner == null) {
			System.err.println("Top owner ID is wrong !");
			return;
		}

		owner.openPrivateChannel().queue(
				channel -> channel.sendMessage(message + ", exception : \r\n" + e.toString()).queue(null, t -> System.err.println("Could not send message to owner : " + message)),
				t -> System.err.println("Could not send message to owner : " + message));
	}

	@Override
	public Consumer<? super Throwable> failureReporter(String message) {
		return t -> reportError(message, t);
	}

	@Override
	public String getAuthorBestName() {
		return getMember().getEffectiveName();
	}

	@SuppressWarnings("ConstantConditions")
	@Nonnull
	@Override
	public Member getMember() {
		return super.getMember();
	}

	@Override
	@NotNull
	public EmbedBuilder getDefaultEmbed() {
		return commandListener.getDefaultEmbedFunction().get();
	}

	@Override
	@Nullable
	public InputStream getDefaultIconStream() {
		return commandListener.getDefaultFooterIconSupplier().get();
	}

	@Override
	public MessageAction sendWithEmbedFooterIcon(MessageEmbed embed, Consumer<? super Throwable> onException) {
		return sendWithEmbedFooterIcon(channel, embed, onException);
	}

	@Override
	@CheckReturnValue
	public MessageAction sendWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed, Consumer<? super Throwable> onException) {
		return sendWithEmbedFooterIcon(channel, getDefaultIconStream(), embed, onException);
	}

	@Override
	@CheckReturnValue
	public MessageAction sendWithEmbedFooterIcon(MessageChannel channel, InputStream iconStream, MessageEmbed embed, Consumer<? super Throwable> onException) {
		if (iconStream != null) {
			final SimpleStream stream = SimpleStream.of(iconStream, onException);
			return new MessageActionWrapper(channel.sendFile(stream, "icon.jpg").embed(embed), onException);
		} else {
			return new MessageActionWrapper(channel.sendMessage(embed), onException);
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

	@CheckReturnValue
	@Nonnull
	public MessageAction reply(@NotNull CharSequence text) {
		return new MessageActionWrapper(channel.sendMessage(text),
				failureReporter(String.format("Failed to reply in channel %s (%d) in guild %s (%d)", channel.getName(), channel.getIdLong(), guild.getName(), guild.getIdLong())));
	}

	@CheckReturnValue
	@Nonnull
	public MessageAction replyFormat(@NotNull String format, @NotNull Object... args) {
		return new MessageActionWrapper(channel.sendMessageFormat(format, args),
				failureReporter(String.format("Failed to reply format in channel %s (%d) in guild %s (%d)", channel.getName(), channel.getIdLong(), guild.getName(), guild.getIdLong())));
	}

	@CheckReturnValue
	@Nonnull
	public MessageAction reply(@NotNull MessageEmbed embed) {
		return new MessageActionWrapper(channel.sendMessage(embed),
				failureReporter(String.format("Failed to reply in channel %s (%d) in guild %s (%d)", channel.getName(), channel.getIdLong(), guild.getName(), guild.getIdLong())));
	}

	@CheckReturnValue
	@Nonnull
	public MessageAction replyFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options) {
		return new MessageActionWrapper(channel.sendFile(data, fileName, options),
				failureReporter(String.format("Failed to reply file in channel %s (%d) in guild %s (%d)", channel.getName(), channel.getIdLong(), guild.getName(), guild.getIdLong())));
	}

	@CheckReturnValue
	@Nonnull
	public MessageAction replyFile(@NotNull byte[] data, @NotNull String fileName, @NotNull AttachmentOption... options) {
		return new MessageActionWrapper(channel.sendFile(data, fileName, options),
				failureReporter(String.format("Failed to reply file in channel %s (%d) in guild %s (%d)", channel.getName(), channel.getIdLong(), guild.getName(), guild.getIdLong())));
	}
}
