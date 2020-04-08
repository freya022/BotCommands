package com.freya02.botcommands;

import com.freya02.botcommands.utils.SimpleStream;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandEvent extends GuildMessageReceivedEvent {
	private static final Pattern idPattern = Pattern.compile("(\\d+)");

	private final CommandListener commandListener;
	private final String argumentsStr;
	private final List<Object> arguments = new ArrayList<>();

	private final List<String> argumentsStrList;

	public CommandEvent(CommandListener commandListener, GuildMessageReceivedEvent event, String arguments) {
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

	public CommandInfo getCommandInfo(String cmdName) {
		return commandListener.getCommandInfo(cmdName);
	}

	public List<Object> getArguments() {
		return arguments;
	}

	public List<String> getArgumentsStrList() {
		return argumentsStrList;
	}

	public void reportError(String message, Throwable e) {
		channel.sendMessage(message).queue(null, t -> System.err.println("Could not send message to channel : " + message));

		final User owner = getJDA().getUserById(commandListener.getOwnerIds().get(0));

		if (owner == null) {
			System.err.println("Top owner ID is wrong !");
			return;
		}

		owner.openPrivateChannel().queue(channel -> {
			channel.sendMessage(message + ", exception : \r\n" + e.toString()).queue(null , t -> System.err.println("Could not send message to owner : " + message));
		}, t -> System.err.println("Could not send message to owner : " + message));
	}

	public Consumer<? super Throwable> failureReporter(String message) {
		return t -> reportError(message, t);
	}

	public <T> boolean hasNext(Class<T> clazz) {
		if (arguments.isEmpty()) {
			return false;
		}

		Object o = arguments.get(0);

		return clazz.isAssignableFrom(o.getClass());
	}

	public <T> T nextIfExists(Class<T> clazz) {
		if (hasNext(clazz)) {
			return nextArgument(clazz);
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T nextArgument(Class<T> clazz) {
		if (arguments.isEmpty()) {
			return null;
		}

		Object o = arguments.remove(0);

		if (clazz.isAssignableFrom(o.getClass())) {
			return (T) o;
		} else {
			return null;
		}
	}

	public EmbedBuilder getDefaultEmbed() {
		return commandListener.getDefaultEmbedFunction().get();
	}

	public InputStream getDefaultIconStream() {
		return commandListener.getDefaultFooterIconSupplier().get();
	}

	public void sendWithEmbedFooterIcon(MessageChannel channel, MessageEmbed embed, Consumer<? super Message> onSuccess, Consumer<? super Throwable> onException) {
		sendWithEmbedFooterIcon(channel, getDefaultIconStream(), embed, onSuccess, onException);
	}

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

	public String getArgumentsStr() {
		return argumentsStr;
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
