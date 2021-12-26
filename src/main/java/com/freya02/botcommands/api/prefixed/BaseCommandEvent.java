package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>Base class containing several utility methods:</p>
 * <ul>
 *     <li>Unhandled exception reporting</li>
 *     <li>Send embeds with footer icon with specified icons / default icon</li>
 *     <li>Sending the command's help embed</li>
 *     <li>Adding a reaction to indicate command success/failure</li>
 * </ul>
 */
public abstract class BaseCommandEvent extends MessageReceivedEvent {
	public BaseCommandEvent(@NotNull JDA api, long responseNumber, @NotNull Message message) {
		super(api, responseNumber, message);
	}

	public abstract BContext getContext();

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
	 * Send an error message to the event's {@linkplain TextChannel} and to the bot owner with the exception name and the simple exception description
	 *
	 * @param message Custom message of what part of the command failed
	 * @param e       The Exception that occurred
	 */
	public abstract void reportError(String message, Throwable e);

	/**
	 * Throwable consumer that, when triggered, sends an error message to the event's {@linkplain TextChannel} and to the bot owner with the exception name and the simple exception description
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
	 * <br>The {@linkplain Member} will never be null as this {@linkplain CommandEvent} is not constructed if the author is a web hook
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
	 * Returns the default embed set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @return Default embed of the bot
	 */
	@NotNull
	public abstract EmbedBuilder getDefaultEmbed();

	/**
	 * Returns the default embed footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @return Default embed footer icon of the bot
	 */
	@Nullable
	public abstract InputStream getDefaultIconStream();

	/**
	 * Sends a {@linkplain MessageEmbed} on the event's channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param embed       {@linkplain MessageEmbed} to send
	 * @param onException Consumer to call when an exception occurred
	 * @return The MessageAction to send
	 */
	@CheckReturnValue
	public abstract RestAction<Message> sendWithEmbedFooterIcon(MessageEmbed embed, Consumer<? super Throwable> onException);

	/**
	 * Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel     {@linkplain MessageChannel} to send the embed in
	 * @param embed       {@linkplain MessageEmbed} to send
	 * @param onException Consumer to call when an exception occurred
	 * @return The MessageAction to send
	 */
	@CheckReturnValue
	public abstract RestAction<Message> sendWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed, Consumer<? super Throwable> onException);

	/**
	 * Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel     {@linkplain MessageChannel} to send the embed in
	 * @param iconStream  InputStream of the footer icon, the input stream is closed once it is unreachable
	 * @param embed       {@linkplain MessageEmbed} to send
	 * @param onException Consumer to call when an exception occurred
	 * @return The MessageAction to send
	 */
	@CheckReturnValue
	public abstract RestAction<Message> sendWithEmbedFooterIcon(MessageChannel channel, InputStream iconStream, MessageEmbed embed, Consumer<? super Throwable> onException);

	/**
	 * Add a :white_check_mark: reaction on the event message to indicate command success
	 *
	 * @return The {@linkplain RestAction} responsible for adding the reaction
	 */
	@CheckReturnValue
	public abstract RestAction<Void> reactSuccess();

	/**
	 * Add a :x: reaction on the event message to indicate a command error
	 *
	 * @return The {@linkplain RestAction} responsible for adding the reaction
	 */
	@CheckReturnValue
	public abstract RestAction<Void> reactError();

	/**
	 * Sends a response in the event's channel
	 *
	 * @param text {@linkplain CharSequence} to send to the event channel
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessage(CharSequence)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> respond(@NotNull CharSequence text);

	/**
	 * Sends a response in the event's channel
	 *
	 * @param format Formatting {@linkplain String} to use for formatting the message sent to the event channel
	 * @param args   Objects to use for formatting
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessageFormat(String, Object...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> respondFormat(@NotNull String format, @NotNull Object... args);

	/**
	 * Sends a response in the event's channel
	 *
	 * @param embed {@linkplain MessageEmbed} to send to the event channel
	 * @param other Additional {@linkplain MessageEmbed embeds} to send
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessageEmbeds(MessageEmbed, MessageEmbed...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> respond(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other);

	/**
	 * Sends a file as a response in the event's channel
	 *
	 * @param data     {@linkplain InputStream} of the data to send
	 * @param fileName Name of the file appearing on Discord
	 * @param options  {@linkplain AttachmentOption AttachmentOptions} for the file sent
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendFile(InputStream, String, AttachmentOption...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> respondFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options);

	/**
	 * Sends a file as a response in the event's channel
	 *
	 * @param data     byte array of the data to send
	 * @param fileName Name of the file appearing on Discord
	 * @param options  {@linkplain AttachmentOption AttachmentOptions} for the file sent
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendFile(byte[], String, AttachmentOption...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> respondFile(@NotNull byte[] data, @NotNull String fileName, @NotNull AttachmentOption... options);

	/**
	 * Sends a reply in the event's channel
	 *
	 * @param text {@linkplain CharSequence} to send to the event channel
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessage(CharSequence)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> reply(@NotNull CharSequence text);

	/**
	 * Sends a reply in the event's channel
	 *
	 * @param format Formatting {@linkplain String} to use for formatting the message sent to the event channel
	 * @param args   Objects to use for formatting
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessageFormat(String, Object...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> replyFormat(@NotNull String format, @NotNull Object... args);

	/**
	 * Sends a reply in the event's channel
	 *
	 * @param embed {@linkplain MessageEmbed} to send to the event channel
	 * @param other Additional {@linkplain MessageEmbed embeds} to send
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessageEmbeds(MessageEmbed, MessageEmbed...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> reply(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other);

	/**
	 * Sends a file as a reply in the event's channel
	 *
	 * @param data     {@linkplain InputStream} of the data to send
	 * @param fileName Name of the file appearing on Discord
	 * @param options  {@linkplain AttachmentOption AttachmentOptions} for the file sent
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendFile(InputStream, String, AttachmentOption...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> replyFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options);

	/**
	 * Sends a file as a reply in the event's channel
	 *
	 * @param data     byte array of the data to send
	 * @param fileName Name of the file appearing on Discord
	 * @param options  {@linkplain AttachmentOption AttachmentOptions} for the file sent
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendFile(byte[], String, AttachmentOption...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> replyFile(@NotNull byte[] data, @NotNull String fileName, @NotNull AttachmentOption... options);

	/**
	 * Sends an error reply in the event's channel
	 *
	 * @param text {@linkplain CharSequence} to send to the event channel
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessage(CharSequence)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> indicateError(@NotNull CharSequence text);

	/**
	 * Sends an error reply in the event's channel
	 *
	 * @param format Formatting {@linkplain String} to use for formatting the message sent to the event channel
	 * @param args   Objects to use for formatting
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessageFormat(String, Object...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> indicateErrorFormat(@NotNull String format, @NotNull Object... args);

	/**
	 * Sends an error reply in the event's channel
	 *
	 * @param embed {@linkplain MessageEmbed} to send to the event channel
	 * @param other Additional {@linkplain MessageEmbed embeds} to send
	 * @return {@linkplain RestAction} to send the message
	 * @see MessageChannel#sendMessageEmbeds(MessageEmbed, MessageEmbed...)
	 */
	@CheckReturnValue
	@NotNull
	public abstract RestAction<Message> indicateError(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other);
}
