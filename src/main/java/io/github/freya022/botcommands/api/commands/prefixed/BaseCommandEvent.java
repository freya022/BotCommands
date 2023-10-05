package io.github.freya022.botcommands.api.commands.prefixed;

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.DefaultEmbedFooterIconSupplier;
import io.github.freya022.botcommands.api.core.DefaultEmbedSupplier;
import io.github.freya022.botcommands.internal.core.BContextImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base text command event containing several utility methods.
 * <ul>
 *     <li>Unhandled exception reporting</li>
 *     <li>Send embeds with footer icon with specified icons / default icon</li>
 *     <li>Sending the command's help embed</li>
 *     <li>Adding a reaction to indicate command success/failure</li>
 * </ul>
 */
public abstract class BaseCommandEvent extends MessageReceivedEvent implements CancellableRateLimit {
	private final BContext context;

	public BaseCommandEvent(@NotNull BContextImpl context, @NotNull JDA api, long responseNumber, @NotNull Message message) {
		super(api, responseNumber, message);

		this.context = context;
	}

	@NotNull
	public BContext getContext() {
		return context;
	}

	/**
	 * Returns the <b>unresolved</b> arguments of the command event
	 *
	 * @return List of String arguments
	 */
	public abstract List<String> getArgumentsStrList();

	/**
	 * Returns the full argument part of the message
	 *
	 * @return Argument part of the message
	 */
	public abstract String getArgumentsStr();

	/**
	 * Send an error message to the event's {@link GuildMessageChannel} and to the bot owner with the exception name and the simple exception description
	 *
	 * @param message Custom message of what part of the command failed
	 * @param e       The Exception that occurred
	 */
	public abstract void reportError(String message, Throwable e);

	/**
	 * Throwable consumer that, when triggered, sends an error message to the event's {@link GuildMessageChannel} and to the bot owner with the exception name and the simple exception description
	 *
	 * @param message Custom message of what part of the command failed
	 * @return A Throwable consumer
	 */
	public abstract Consumer<? super Throwable> failureReporter(String message);

	/**
	 * <p>Returns the best author name possible</p>
	 * <p>If the User is not in the guild then returns his tag (Name#Discriminator)</p>
	 * <p>If the User is in the guild then returns his effective name</p>
	 *
	 * @return The best way to describe someone's name
	 */
	public abstract String getAuthorBestName();

	/**
	 * The Author of the Message received as {@link net.dv8tion.jda.api.entities.Member Member} object.
	 * <br>The {@link Member} will never be null as this {@link CommandEvent} is not constructed if the author is a web hook
	 *
	 * @return The Author of the Message as Member object.
	 */
	@SuppressWarnings("ConstantConditions")
	@NotNull
	@Override
	public Member getMember() {
		return super.getMember();
	}

	/**
	 * Returns the default embed, equivalent to {@link BContext#getDefaultEmbedSupplier() BContext.getDefaultEmbedSupplier().get()}
	 *
	 * @return Default embed of the bot
	 */
	@NotNull
	public abstract EmbedBuilder getDefaultEmbed();

	/**
	 * Returns the default embed footer icon, equivalent to {@link BContext#getDefaultFooterIconSupplier() BContext.getDefaultFooterIconSupplier().get()}
	 *
	 * @return Default embed footer icon of the bot
	 */
	@Nullable
	public abstract InputStream getDefaultIconStream();

	/**
	 * Sends a {@link MessageEmbed} on the event's channel with the default footer icon
	 *
	 * @param embed       {@link MessageEmbed} to send
	 * @param onException Consumer to call when an exception occurred
	 *
	 * @return The RestAction of the Message to send
	 *
	 * @see DefaultEmbedSupplier
	 * @see DefaultEmbedFooterIconSupplier
	 */
	@CheckReturnValue
	public abstract RestAction<Message> sendWithEmbedFooterIcon(MessageEmbed embed, Consumer<? super Throwable> onException);

	/**
	 * Sends a {@link MessageEmbed} on the specified channel with the default footer icon
	 *
	 * @param channel     {@link MessageChannel} to send the embed in
	 * @param embed       {@link MessageEmbed} to send
	 * @param onException Consumer to call when an exception occurred
	 *
	 * @return The RestAction of the Message to send
	 *
	 * @see DefaultEmbedSupplier
	 * @see DefaultEmbedFooterIconSupplier
	 */
	@CheckReturnValue
	public abstract RestAction<Message> sendWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed, Consumer<? super Throwable> onException);

	/**
	 * Sends a {@link MessageEmbed} on the specified channel with the default footer icon
	 *
	 * @param channel     {@link MessageChannel} to send the embed in
	 * @param iconStream  InputStream of the footer icon, the input stream is closed once it is unreachable
	 * @param embed       {@link MessageEmbed} to send
	 * @param onException Consumer to call when an exception occurred
	 *
	 * @return The RestAction of the Message to send
	 *
	 * @see DefaultEmbedSupplier
	 * @see DefaultEmbedFooterIconSupplier
	 */
	@CheckReturnValue
	public abstract RestAction<Message> sendWithEmbedFooterIcon(MessageChannel channel, InputStream iconStream, MessageEmbed embed, Consumer<? super Throwable> onException);

	/**
	 * Add a :white_check_mark: reaction on the event message to indicate command success
	 *
	 * @return The {@link RestAction} responsible for adding the reaction
	 */
	@CheckReturnValue
	public abstract RestAction<Void> reactSuccess();

	/**
	 * Add a :x: reaction on the event message to indicate a command error
	 *
	 * @return The {@link RestAction} responsible for adding the reaction
	 */
	@CheckReturnValue
	public abstract RestAction<Void> reactError();

	/**
	 * Sends a response in the event's channel
	 *
	 * @param text {@link CharSequence} to send to the event channel
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessage(CharSequence)
	 */
	@CheckReturnValue
	@NotNull
	public abstract MessageCreateAction respond(@NotNull CharSequence text);

	/**
	 * Sends a response in the event's channel
	 *
	 * @param format Formatting {@link String} to use for formatting the message sent to the event channel
	 * @param args   Objects to use for formatting
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessageFormat(String, Object...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract MessageCreateAction respondFormat(@NotNull String format, @NotNull Object... args);

	/**
	 * Sends a response in the event's channel
	 *
	 * @param embed {@link MessageEmbed} to send to the event channel
	 * @param other Additional {@link MessageEmbed embeds} to send
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessageEmbeds(MessageEmbed, MessageEmbed...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract MessageCreateAction respond(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other);

	/**
	 * Sends a file as a response in the event's channel
	 *
	 * @param fileUploads The {@link FileUpload file uploads} to send
	 *
	 * @return {@link RestAction} to send the message
	 *
	 * @see MessageChannel#sendFiles(FileUpload...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract MessageCreateAction respondFile(@NotNull FileUpload... fileUploads);

	/**
	 * Sends a reply in the event's channel
	 *
	 * @param text {@link CharSequence} to send to the event channel
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessage(CharSequence)
	 */
	@CheckReturnValue
	@NotNull
	public abstract MessageCreateAction reply(@NotNull CharSequence text);

	/**
	 * Sends a reply in the event's channel
	 *
	 * @param format Formatting {@link String} to use for formatting the message sent to the event channel
	 * @param args   Objects to use for formatting
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessageFormat(String, Object...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract MessageCreateAction replyFormat(@NotNull String format, @NotNull Object... args);

	/**
	 * Sends a reply in the event's channel
	 *
	 * @param embed {@link MessageEmbed} to send to the event channel
	 * @param other Additional {@link MessageEmbed embeds} to send
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessageEmbeds(MessageEmbed, MessageEmbed...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract MessageCreateAction reply(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other);

	/**
	 * Sends a file as a reply in the event's channel
	 *
	 * @param fileUploads The {@link FileUpload file uploads} to send
	 *
	 * @return {@link RestAction} to send the message
	 *
	 * @see MessageChannel#sendFiles(FileUpload...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> replyFile(@NotNull FileUpload... fileUploads);

	/**
	 * Sends an error reply in the event's channel
	 *
	 * @param text {@link CharSequence} to send to the event channel
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessage(CharSequence)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> indicateError(@NotNull CharSequence text);

	/**
	 * Sends an error reply in the event's channel
	 *
	 * @param format Formatting {@link String} to use for formatting the message sent to the event channel
	 * @param args   Objects to use for formatting
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessageFormat(String, Object...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> indicateErrorFormat(@NotNull String format, @NotNull Object... args);

	/**
	 * Sends an error reply in the event's channel
	 *
	 * @param embed {@link MessageEmbed} to send to the event channel
	 * @param other Additional {@link MessageEmbed embeds} to send
	 * @return {@link RestAction} to send the message
	 * @see MessageChannel#sendMessageEmbeds(MessageEmbed, MessageEmbed...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> indicateError(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other);
}
