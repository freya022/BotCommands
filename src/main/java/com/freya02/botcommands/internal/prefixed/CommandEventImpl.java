package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.exceptions.BadIdException;
import com.freya02.botcommands.api.prefixed.exceptions.NoIdException;
import com.freya02.botcommands.api.utils.RichTextFinder;
import com.freya02.botcommands.api.utils.RichTextType;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.entities.EmojiImpl;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandEventImpl extends CommandEvent {
	private static final Pattern idPattern = Pattern.compile("(\\d+)");
	private static final Logger LOGGER = Logging.getLogger();

	private final List<Object> arguments = new ArrayList<>();

	public CommandEventImpl(BContext context, GuildMessageReceivedEvent event, String arguments) {
		super(context, event, arguments);

		new RichTextFinder(arguments, true, false, true, false).processResults(this::processText);
	}

	private static IMentionable tryGetId(String mention, Function<Long, IMentionable> idToMentionableFunc) {
		Matcher matcher = idPattern.matcher(mention);
		if (matcher.find()) {
			return idToMentionableFunc.apply(Long.valueOf(matcher.group()));
		}

		return null;
	}

	@Override
	public List<Object> getArguments() {
		return arguments;
	}

	@Override
	public <T> boolean hasNext(Class<T> clazz) {
		if (arguments.isEmpty()) {
			return false;
		}

		Object o = arguments.get(0);

		return clazz.isAssignableFrom(o.getClass());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T peekArgument(Class<T> clazz) {
		if (arguments.isEmpty()) {
			throw new NoSuchElementException();
		}

		Object o = arguments.get(0);

		if (clazz.isAssignableFrom(o.getClass())) {
			return (T) o;
		} else {
			throw new NoSuchElementException();
		}
	}

	@Override
	@NotNull
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

	@Override
	@NotNull
	@SuppressWarnings({"unchecked"})
	public <T extends IMentionable> T resolveNext(Class<?>... classes) throws NoIdException, BadIdException {
		if (arguments.isEmpty()) {
			throw new NoIdException();
		}

		Object o = arguments.remove(0);

		for (Class<?> c : classes) {
			if (c.isAssignableFrom(o.getClass())) {
				return (T) o;
			}
		}
		if (!(o instanceof String)) throw new NoIdException();

		final String idStr = (String) o;
		for (Class<?> clazz : classes) {
			final IMentionable mentionable;

			try {
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
				} else if (clazz == Emote.class) {
					mentionable = getJDA().getEmoteById(id);
				} else {
					throw new IllegalArgumentException(clazz.getSimpleName() + " is not a valid IMentionable class");
				}

				if (mentionable != null) {
					return (T) mentionable;
				}
			} catch (NumberFormatException ignored) {
				throw new BadIdException();
			} catch (ErrorResponseException e) {
				if (e.getErrorResponse() == ErrorResponse.UNKNOWN_USER || e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
					throw new BadIdException();
				} else {
					throw e;
				}
			}
		}

		throw new BadIdException();
	}

	private void processText(String substring, RichTextType type) {
		if (substring.isBlank()) return;

		Message.MentionType mentionType = type.getMentionType();
		if (mentionType != null || type == RichTextType.UNICODE_EMOTE) {
			Object mentionable = null;

			if (type == RichTextType.UNICODE_EMOTE) {
				mentionable = new EmojiImpl(substring);
			} else if (mentionType == Message.MentionType.ROLE) {
				mentionable = tryGetId(substring, id -> getGuild().getRoleById(id));
			} else if (mentionType == Message.MentionType.CHANNEL) {
				mentionable = tryGetId(substring, id -> getGuild().getTextChannelById(id));
			} else if (mentionType == Message.MentionType.EMOTE) {
				final Matcher matcher = Message.MentionType.EMOTE.getPattern().matcher(substring);
				if (matcher.find()) {
					String id = matcher.group(2);
					mentionable = getGuild().getEmoteById(id);
				}
			} else if (mentionType == Message.MentionType.USER) {
				mentionable = tryGetId(substring, id -> getJDA().getUserById(id));
			}

			if (mentionable != null) {
				this.arguments.add(mentionable);
			} else {
				LOGGER.error("Unresolved mentionable : '{}' of type {}, maybe you haven't enabled a cache flag / intent ?", substring, type.name());
			}
		} else if (!substring.isEmpty()) {
			Collections.addAll(arguments, substring.split(" "));
		}
	}
}
