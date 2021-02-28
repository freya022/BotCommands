package com.freya02.botcommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IBaseCommandEvent {
	/**
	 * Shows the help content on the current invoked command
	 */
	void showHelp();

	/**
	 * Returns a list of IDs of the bot owners
	 * @return a list of IDs of the bot owners
	 */
	List<Long> getOwnerIds();

	/**
	 * Returns the {@linkplain CommandInfo} object of the specified command, the name can be an alias too
	 *
	 * @param cmdName Name / alias of the command
	 * @return The {@linkplain CommandInfo} object of the command
	 */
	@Nullable CommandInfo getCommandInfo(String cmdName);

	/**
	 * Returns the <b>unresolved</b> arguments of the command event
	 *
	 * @return List of String arguments
	 */
	List<String> getArgumentsStrList();

	/**
	 * Returns the full argument part of the message
	 *
	 * @return Argument part of the message
	 */
	String getArgumentsStr();

	/**
	 * Send an error message to the event's {@linkplain TextChannel} and to the bot owner with the exception name and the simple exception description
	 *
	 * @param message Custom message of what part of the command failed
	 * @param e       The Exception that occurred
	 */
	void reportError(String message, Throwable e);

	/**
	 * Throwable consumer that, when triggered, sends an error message to the event's {@linkplain TextChannel} and to the bot owner with the exception name and the simple exception description
	 *
	 * @param message Custom message of what part of the command failed
	 * @return A Throwable consumer
	 */
	Consumer<? super Throwable> failureReporter(String message);

	/**
	 * <p>Returns the best author name possible</p>
	 * <p>If the User is not in the guild then returns his tag (Name#Discriminator)</p>
	 * <p>If the User is in the guild then returns his effective name</p>
	 *
	 * @return The best way to describe someone's name
	 */
	String getAuthorBestName();

	/**
	 * The Author of the Message received as {@link net.dv8tion.jda.api.entities.Member Member} object.
	 * <br>The {@linkplain Member} will never be null as this {@linkplain CommandEvent} is not constructed if the author is a web hook
	 *
	 * @return The Author of the Message as Member object.
	 */
	@Nonnull
	Member getMember();

	/**
	 * Returns the default embed set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @return Default embed of the bot
	 */
	@NotNull EmbedBuilder getDefaultEmbed();

	/**
	 * Returns the default embed footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @return Default embed footer icon of the bot
	 */
	@Nullable InputStream getDefaultIconStream();

	/**
	 * Sends a {@linkplain MessageEmbed} on the event's channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param embed       {@linkplain MessageEmbed} to send
	 * @param onException Consumer to call when an exception occurred
	 * @return The MessageAction to send
	 */
	@CheckReturnValue
	RestAction<Message> sendWithEmbedFooterIcon(MessageEmbed embed, Consumer<? super Throwable> onException);

	/**
	 * Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel     {@linkplain MessageChannel} to send the embed in
	 * @param embed       {@linkplain MessageEmbed} to send
	 * @param onException Consumer to call when an exception occurred
	 * @return The MessageAction to send
	 */
	@CheckReturnValue
	RestAction<Message> sendWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed, Consumer<? super Throwable> onException);

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
	RestAction<Message> sendWithEmbedFooterIcon(MessageChannel channel, InputStream iconStream, MessageEmbed embed, Consumer<? super Throwable> onException);

	/**
	 * Add a :white_check_mark: reaction on the event message to indicate command success
	 *
	 * @return The {@linkplain RestAction} responsible for adding the reaction
	 */
	@CheckReturnValue
	RestAction<Void> reactSuccess();

	/**
	 * Add a :x: reaction on the event message to indicate a command error
	 *
	 * @return The {@linkplain RestAction} responsible for adding the reaction
	 */
	@CheckReturnValue
	RestAction<Void> reactError();

	@CheckReturnValue
	@Nonnull
	RestAction<Message> reply(@NotNull CharSequence text);

	@CheckReturnValue
	@Nonnull
	RestAction<Message> replyFormat(@NotNull String format, @NotNull Object... args);

	@CheckReturnValue
	@Nonnull
	RestAction<Message> reply(@NotNull MessageEmbed embed);

	@CheckReturnValue
	@Nonnull
	RestAction<Message> replyFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options);

	@CheckReturnValue
	@Nonnull
	RestAction<Message> replyFile(@NotNull byte[] data, @NotNull String fileName, @NotNull AttachmentOption... options);
}
