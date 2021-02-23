package com.freya02.botcommands;

import com.freya02.botcommands.utils.SimpleStream;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BaseCommandEvent extends GuildMessageReceivedEvent {
	protected final CommandListener commandListener;
	protected final String argumentsStr;

	BaseCommandEvent(CommandListener commandListener, GuildMessageReceivedEvent event, String arguments) {
		super(event.getJDA(), event.getResponseNumber(), event.getMessage());
		this.commandListener = commandListener;
		this.argumentsStr = arguments;
	}

	public List<Long> getOwnerIds() {
		return commandListener.getOwnerIds();
	}

	/**
	 * Returns the {@linkplain CommandInfo} object of the specified command, the name can be an alias too
	 *
	 * @param cmdName Name / alias of the command
	 * @return The {@linkplain CommandInfo} object of the command
	 */
	@Nullable
	public CommandInfo getCommandInfo(String cmdName) {
		return commandListener.getCommandInfo(cmdName);
	}

	/**
	 * Returns the <b>unresolved</b> arguments of the command event
	 *
	 * @return List of String arguments
	 */
	public List<String> getArgumentsStrList() {
		if (!argumentsStr.isBlank()) {
			return Arrays.asList(argumentsStr.split(" "));
		} else {
			return List.of();
		}
	}

	/**
	 * Returns the full argument part of the message
	 *
	 * @return Argument part of the message
	 */
	public String getArgumentsStr() {
		return argumentsStr;
	}

	/**
	 * Send an error message to the event's {@linkplain TextChannel} and to the bot owner with the exception name and the simple exception description
	 *
	 * @param message Custom message of what part of the command failed
	 * @param e       The Exception that occurred
	 */
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

	/**
	 * Throwable consumer that, when triggered, sends an error message to the event's {@linkplain TextChannel} and to the bot owner with the exception name and the simple exception description
	 *
	 * @param message Custom message of what part of the command failed
	 * @return A Throwable consumer
	 */
	public Consumer<? super Throwable> failureReporter(String message) {
		return t -> reportError(message, t);
	}

	/**
	 * <p>Returns the best author name possible</p>
	 * <p>If the User is not in the guild then returns his tag (Name#Discriminator)</p>
	 * <p>If the User is in the guild then returns his effective name</p>
	 *
	 * @return The best way to describe someone's name
	 */
	public String getAuthorBestName() {
		return getMember().getEffectiveName();
	}

	/**
	 * The Author of the Message received as {@link net.dv8tion.jda.api.entities.Member Member} object.
	 * <br>The {@linkplain Member} will never be null as this {@linkplain CommandEvent} is not constructed if the author is a web hook
	 *
	 * @return The Author of the Message as Member object.
	 */
	@SuppressWarnings("ConstantConditions")
	@Nonnull
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
	public EmbedBuilder getDefaultEmbed() {
		return commandListener.getDefaultEmbedFunction().get();
	}

	/**
	 * Returns the default embed footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @return Default embed footer icon of the bot
	 */
	@Nullable
	public InputStream getDefaultIconStream() {
		return commandListener.getDefaultFooterIconSupplier().get();
	}

	/**
	 * Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel     {@linkplain MessageChannel} to send the embed in
	 * @param embed       {@linkplain MessageEmbed} to send
	 * @param onSuccess   Consumer to call when the embed has been successfully sent
	 * @param onException Consumer to call when an exception occurred
	 */
	public void sendWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed, Consumer<? super Message> onSuccess, Consumer<? super Throwable> onException) {
		sendWithEmbedFooterIcon(channel, getDefaultIconStream(), embed, onSuccess, onException);
	}

	/**
	 * Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel     {@linkplain MessageChannel} to send the embed in
	 * @param iconStream  InputStream of the footer icon, the input stream is closed upon success / error
	 * @param embed       {@linkplain MessageEmbed} to send
	 * @param onSuccess   Consumer to call when the embed has been successfully sent
	 * @param onException Consumer to call when an exception occurred
	 */
	public void sendWithEmbedFooterIcon(MessageChannel channel, InputStream iconStream, MessageEmbed embed, Consumer<? super Message> onSuccess, Consumer<? super Throwable> onException) {
		if (iconStream != null) {
			final SimpleStream stream = SimpleStream.of(iconStream, onException);
			channel.sendFile(stream, "icon.jpg").embed(embed).queue(x -> {
				stream.close();
				if (onSuccess != null) {
					onSuccess.accept(x);
				}
			}, t -> {
				stream.close();
				if (onException != null) {
					onException.accept(t);
				}
			});
		} else {
			channel.sendMessage(embed).queue(x -> {
				if (onSuccess != null) {
					onSuccess.accept(x);
				}
			}, t -> {
				if (onException != null) {
					onException.accept(t);
				}
			});
		}
	}

	/**
	 * Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel {@linkplain MessageChannel} to send the embed in
	 * @param embed   {@linkplain MessageEmbed} to send
	 * @return The sent message
	 */
	public Message completeWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed) {
		return completeWithEmbedFooterIcon(channel, getDefaultIconStream(), embed);
	}

	/**
	 * Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel    {@linkplain MessageChannel} to send the embed in
	 * @param iconStream InputStream of the footer icon, the input stream is closed upon success / error
	 * @param embed      {@linkplain MessageEmbed} to send
	 * @return The sent message
	 */
	public Message completeWithEmbedFooterIcon(MessageChannel channel, InputStream iconStream, MessageEmbed embed) {
		final Message message;
		try {
			message = channel.sendFile(iconStream, "icon.jpg").embed(embed).complete();
		} finally {
			try {
				iconStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return message;
	}
}
