package com.freya02.botcommands;

import com.freya02.botcommands.exceptions.BadIdException;
import com.freya02.botcommands.exceptions.NoIdException;
import com.freya02.botcommands.utils.SimpleStream;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Contains all the information about the triggered command</p>
 * <p>Arguments are tokenized and resolved into entities if possible</p>
 * <p>Also contains utility methods for fast error handling, default embeds specified in {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier) setDefaultEmbedFunction(...)}, and embed sender with the default icon stream</p>
 */
public class CommandEvent extends GuildMessageReceivedEvent {
	private static final Pattern idPattern = Pattern.compile("(\\d+)");

	private final CommandListener commandListener;
	private final String argumentsStr;
	private final List<Object> arguments = new ArrayList<>();

	private final List<String> argumentsStrList;

	CommandEvent(CommandListener commandListener, GuildMessageReceivedEvent event, String arguments) {
		super(event.getJDA(), event.getResponseNumber(), event.getMessage());
		this.commandListener = commandListener;
		argumentsStr = arguments;

		if (!arguments.isBlank()) {
			argumentsStrList = Arrays.asList(arguments.split(" "));
		} else {
			argumentsStrList = List.of();
		}
		for (String argument : argumentsStrList) {
			processMessageBody(argument);
		}
	}

	/** Returns the {@linkplain CommandInfo} object of the specified command, the name can be an alias too
	 * @param cmdName Name / alias of the command
	 * @return The {@linkplain CommandInfo} object of the command
	 */
	@Nullable
	public CommandInfo getCommandInfo(String cmdName) {
		return commandListener.getCommandInfo(cmdName);
	}

	/** Returns the <b>resolved</b> arguments of the command event, these can be a {@linkplain User}, {@linkplain Role}, {@linkplain TextChannel} or a {@linkplain String}
	 * @return List of arguments
	 */
	public List<Object> getArguments() {
		return arguments;
	}

	/** Returns the <b>unresolved</b> arguments of the command event
	 * @return List of String arguments
	 */
	public List<String> getArgumentsStrList() {
		return argumentsStrList;
	}

	/** Returns the full argument part of the message
	 * @return Argument part of the message
	 */
	public String getArgumentsStr() {
		return argumentsStr;
	}

	/** Send an error message to the event's {@linkplain TextChannel} and to the bot owner with the exception name and the simple exception description
	 * @param message Custom message of what part of the command failed
	 * @param e The Exception that occurred
	 */
	public void reportError(String message, Throwable e) {
		channel.sendMessage(message).queue(null, t -> System.err.println("Could not send message to channel : " + message));

		final User owner = getJDA().getUserById(commandListener.getOwnerIds().get(0));

		if (owner == null) {
			System.err.println("Top owner ID is wrong !");
			return;
		}

		owner.openPrivateChannel().queue(
				channel -> channel.sendMessage(message + ", exception : \r\n" + e.toString()).queue(null , t -> System.err.println("Could not send message to owner : " + message)),
				t -> System.err.println("Could not send message to owner : " + message));
	}

	/** Throwable consumer that, when triggered, sends an error message to the event's {@linkplain TextChannel} and to the bot owner with the exception name and the simple exception description
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
	 * @return The best way to describe someone's name
	 */
	public String getAuthorBestName() {
		return getMember().getEffectiveName();
	}

	/** The Author of the Message received as {@link net.dv8tion.jda.api.entities.Member Member} object.
	 * <br>The {@linkplain Member} will never be null as this {@linkplain CommandEvent} is not constructed if the author is a web hook
	 *
	 * @return The Author of the Message as Member object.
	 *
	 * @see    #isWebhookMessage()
	 */
	@SuppressWarnings("ConstantConditions")
	@Nonnull
	@Override
	public Member getMember() {
		return super.getMember();
	}

	/** Checks if the next argument exists and is of type T, returns <code>true</code> if so
	 * @param clazz Class of the requested type
	 * @param <T> Type of the requested argument
	 * @return <code>true</code> if the argument exists, <code>false</code> if not
	 */
	public <T> boolean hasNext(Class<T> clazz) {
		if (arguments.isEmpty()) {
			return false;
		}

		Object o = arguments.get(0);

		return clazz.isAssignableFrom(o.getClass());
	}

	/*public <T> T nextIfExists(Class<T> clazz) {
		if (hasNext(clazz)) {
			return nextArgument(clazz);
		}

		return null;
	}*/

	/** Returns the next argument if it is of type T
	 * @param clazz Class of the requested type
	 * @param <T> Type of the requested argument
	 * @return The argument of type T if it exists
	 * @throws NoSuchElementException In case there is no more arguments to be read
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> T nextArgument(Class<T> clazz) {
		if (arguments.isEmpty()) {
			throw new NoSuchElementException();
		}

		Object o = arguments.remove(0);

		if (clazz.isAssignableFrom(o.getClass())) {
			return (T) o;
		} else {
			throw new NoSuchElementException();
		}
	}

	/** Returns the next IMentionable
	 * @param clazz Class of the requested type
	 * @param <T> Type of the requested argument
	 * @return The argument of type T, extending IMentionable, if it exists
	 * @throws BadIdException In case the ID is not a valid snowflake, or does not refer to a known IMentionable
	 * @throws NoIdException In case there is no ID / IMentionable in the message
	 * @throws NoSuchElementException In case there is no more arguments to be read, or the type isn't the same
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T extends IMentionable> T resolveNext(Class<T> clazz) throws NoIdException, BadIdException {
		if (arguments.isEmpty()) {
			throw new NoSuchElementException();
		}

		Object o = arguments.remove(0);

		if (clazz.isAssignableFrom(o.getClass())) {
			return (T) o;
		} else {
			if (o instanceof String) {
				final IMentionable mentionable;

				try {
					final String idStr = (String) o;

					//See net.dv8tion.jda.internal.utils.Checks#isSnowflake(String)
					if (idStr.length() > 20 || !Helpers.isNumeric(idStr)) {
						throw new BadIdException();
					}

					final long id = Long.parseLong(idStr);

					if (clazz == Role.class) {
						mentionable = getGuild().getRoleById(id);
					} else if (clazz == User.class) {
						mentionable = getJDA().retrieveUserById(id).complete();
					} else if (clazz == Member.class) {
						mentionable = getGuild().retrieveMemberById(id).complete();
					} else if (clazz == TextChannel.class) {
						mentionable = getGuild().getTextChannelById(id);
					} else {
						throw new IllegalArgumentException(clazz.getSimpleName() + " is not a valid IMentionable class");
					}

					if (mentionable == null) {
						throw new BadIdException();
					}

					return (T) mentionable;
				} catch (NumberFormatException ignored) {
					throw new BadIdException();
				}
			} else {
				throw new NoIdException();
			}
		}
	}

	/** Returns the default embed set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 * @return Default embed of the bot
	 */
	@NotNull
	public EmbedBuilder getDefaultEmbed() {
		return commandListener.getDefaultEmbedFunction().get();
	}

	/** Returns the default embed footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 * @return Default embed footer icon of the bot
	 */
	@NotNull
	public InputStream getDefaultIconStream() {
		return commandListener.getDefaultFooterIconSupplier().get();
	}

	/** Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel {@linkplain MessageChannel} to send the embed in
	 * @param embed {@linkplain MessageEmbed} to send
	 * @param onSuccess Consumer to call when the embed has been successfully sent
	 * @param onException Consumer to call when an exception occurred
	 */
	public void sendWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed, Consumer<? super Message> onSuccess, Consumer<? super Throwable> onException) {
		sendWithEmbedFooterIcon(channel, getDefaultIconStream(), embed, onSuccess, onException);
	}

	/** Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel {@linkplain MessageChannel} to send the embed in
	 * @param iconStream InputStream of the footer icon, the input stream is closed upon success / error
	 * @param embed {@linkplain MessageEmbed} to send
	 * @param onSuccess Consumer to call when the embed has been successfully sent
	 * @param onException Consumer to call when an exception occurred
	 */
	public void sendWithEmbedFooterIcon(MessageChannel channel, InputStream iconStream, MessageEmbed embed, Consumer<? super Message> onSuccess, Consumer<? super Throwable> onException) {
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
	}

	/** Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel {@linkplain MessageChannel} to send the embed in
	 * @param embed {@linkplain MessageEmbed} to send
	 * @return The sent message
	 */
	public Message completeWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed) {
		return completeWithEmbedFooterIcon(channel, getDefaultIconStream(), embed);
	}

	/** Sends a {@linkplain MessageEmbed} on the specified channel with the default footer icon set by {@linkplain CommandsBuilder#setDefaultEmbedFunction(Supplier, Supplier)}
	 *
	 * @param channel {@linkplain MessageChannel} to send the embed in
	 * @param iconStream InputStream of the footer icon, the input stream is closed upon success / error
	 * @param embed {@linkplain MessageEmbed} to send
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

	private static IMentionable tryGetId(String mention, Function<Long, IMentionable> idToMentionableFunc) {
		Matcher matcher = idPattern.matcher(mention);
		if (matcher.find()) {
			return idToMentionableFunc.apply(Long.valueOf(matcher.group()));
		}

		return null;
	}

	private void processSubstring(Map<String, Message.MentionType> mentions, String substring) {
		Message.MentionType mentionType;
		if ((mentionType = mentions.get(substring)) != null) {
			IMentionable mentionable = null;

			if (mentionType == Message.MentionType.USER) {
				mentionable = tryGetId(substring, id -> getJDA().getUserById(id));
			} else if (mentionType == Message.MentionType.ROLE) {
				mentionable = tryGetId(substring, id -> getGuild().getRoleById(id));
			} else if (mentionType == Message.MentionType.CHANNEL) {
				mentionable = tryGetId(substring, id -> getGuild().getTextChannelById(id));
			}

			if (mentionable != null) {
				arguments.add(mentionable);
			} else {
				System.err.println("Unresolved mentionable : " + substring);
			}
		} else if (!substring.isEmpty()) {
			arguments.add(substring);
		}
	}

	private void processMessageBody(String str) {
		MentionCut cut = new MentionCut(str);

		Set<Integer> indexes = cut.getIndexes();
		Map<String, Message.MentionType> mentions = cut.getMentions();

		int startIndex = 0;
		int endIndex = 0;
		for (int index : indexes) {
			endIndex = index;

			String substring = str.substring(startIndex, endIndex);
			processSubstring(mentions, substring);

			startIndex = endIndex;
		}

		String substring = str.substring(endIndex);
		processSubstring(mentions, substring);
	}

	private static class MentionCut {
		private final String input;
		private final Set<Integer> indexes = new TreeSet<>();
		private final Map<String, Message.MentionType> mentions = new HashMap<>();

		private MentionCut(String input) {
			this.input = input;

			findAllMentions(Message.MentionType.USER);
			findAllMentions(Message.MentionType.CHANNEL);
			findAllMentions(Message.MentionType.EMOTE);
			findAllMentions(Message.MentionType.ROLE);
		}

		private void findAllMentions(Message.MentionType type) {
			Matcher matcher = type.getPattern().matcher(input);
			while (matcher.find()) {
				indexes.add(matcher.start());
				indexes.add(matcher.end());
				mentions.put(matcher.group(), type);
			}
		}

		private Set<Integer> getIndexes() {
			return indexes;
		}

		private Map<String, Message.MentionType> getMentions() {
			return mentions;
		}
	}
}
