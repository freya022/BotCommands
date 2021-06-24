package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.buttons.annotation.JdaButtonListener;
import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static com.freya02.botcommands.buttons.ButtonsBuilder.buttonsMap;

//ID format :
// 0|oneUse|callerId|handlerName|args
// OR
// 1|oneUse|callerId|handlerId

/**
 * Class for creating button IDs for Discord, when clicked they are handled by {@linkplain ButtonListener}
 * <br><br>
 * <b>For advanced users:</b><br>
 * You can delete the buttons yourself if you don't need them anymore by using {@link IdManager#removeId(String, boolean)} or {@link IdManager#removeIds(Collection)}
 */
public class ButtonId {
	private static final Logger LOGGER = Logging.getLogger();
	private static final List<Class<? extends ISnowflake>> RESTRICTED_CLASSES = List.of(Role.class, AbstractChannel.class, Guild.class, Emote.class, User.class, Message.class);
	private static BContextImpl context;

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}
	 *
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args        The objects objects to use in the string
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String of(String handlerName, Object... args) {
		return create(handlerName, false, 0L, args);
	}

	/**
	 * Creates a button ID and associates it with the consumer
	 *
	 * @param action Consumer used when the button is clicked
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String of(ButtonConsumer action) {
		return create(false, 0L, action);
	}

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}<br>
	 * <b>This button is only usable once</b>
	 *
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args        The objects objects to use in the string
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String uniqueOf(String handlerName, Object... args) {
		return create(handlerName, true, 0L, args);
	}

	/**
	 * Creates a button ID and associates it with the consumer<br>
	 * <b>This button is only usable once</b>
	 *
	 * @param action Consumer used when the button is clicked
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String uniqueOf(ButtonConsumer action) {
		return create(true, 0L, action);
	}

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}<br>
	 * <b>The action won't execute unless the user's who clicked the button is of the supplied ID</b>
	 *
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args        The objects objects to use in the string
	 * @param callerId    ID of the user from which to only accept clicks from
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String ofUser(String handlerName, long callerId, Object... args) {
		return create(handlerName, false, callerId, args);
	}

	/**
	 * Creates a button ID and associates it with the consumer<br>
	 * <b>The action won't execute unless the user's who clicked the button is of the supplied ID</b>
	 *
	 * @param callerId ID of the user from which to only accept clicks from
	 * @param action   Consumer used when the button is clicked
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String ofUser(long callerId, ButtonConsumer action) {
		return create(false, callerId, action);
	}

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}<br>
	 * <b>The action won't execute unless the user's who clicked the button is of the supplied ID</b><br>
	 * <b>This button is only usable once</b>
	 *
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args        The objects objects to use in the string
	 * @param callerId    ID of the user from which to only accept clicks from
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String uniqueOfUser(String handlerName, long callerId, Object... args) {
		return create(handlerName, true, callerId, args);
	}

	/**
	 * Creates a button ID and associates it with the consumer<br>
	 * <b>The action won't execute unless the user's who clicked the button is of the supplied ID</b><br>
	 * <b>This button is only usable once</b>
	 *
	 * @param callerId ID of the user from which to only accept clicks from
	 * @param action   Consumer used when the button is clicked
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String uniqueOfUser(long callerId, ButtonConsumer action) {
		return create(true, callerId, action);
	}

	private static String create(String handlerName, boolean oneUse, long callerId, Object... args) {
		if (!buttonsMap.containsKey(handlerName))
			throw new IllegalStateException("Button listener with name '" + handlerName + "' doesn't exist");

		final String constructedId = constructId(handlerName, oneUse, callerId, args);

		return Objects.requireNonNull(context.getIdManager(), "ID Manager should be set in order to use Discord components").newId(constructedId, false);
	}

	private static String create(boolean oneUse, long callerId, ButtonConsumer action) {
		final IdManager idManager = Objects.requireNonNull(context.getIdManager(), "ID Manager should be set in order to use Discord components");

		for (Field field : action.getClass().getDeclaredFields()) {
			for (Class<?> aClass : RESTRICTED_CLASSES) {
				if (aClass.isAssignableFrom(field.getType())) {
					LOGGER.warn("A button consumer has a field of type {}, these objects could be invalid when the action is called. Consider having IDs of the objects you need, refer to https://github.com/DV8FromTheWorld/JDA/wiki/19)-Troubleshooting#cannot-get-reference-as-it-has-already-been-garbage-collected", aClass.getSimpleName());
				}
			}
		}

		final int handlerId = idManager.newHandlerId(action);

		final String content = constructId(oneUse, callerId, handlerId);

		return idManager.newId(content, true);
	}

	@Nonnull
	static String constructId(String handlerName, boolean oneUse, long callerId, Object[] args) {
		StringJoiner idBuilder = new StringJoiner("|")
				.add("0")
				.add(oneUse ? "1" : "0")
				.add(String.valueOf(callerId))
				.add(escape(handlerName));
		for (Object arg : args) {
			final String s;
			if (arg instanceof ISnowflake) {
				s = ((ISnowflake) arg).getId();
			} else {
				s = escape(arg.toString());
			}

			idBuilder.add(s);
		}

		return idBuilder.toString();
	}

	@Nonnull
	static String constructId(boolean oneUse, long callerId, int handlerId) {
		return "1|" + (oneUse ? '1' : '0') + '|' + callerId + '|' + handlerId;
	}

	static String escape(String str) {
		return str.replace("|", "\\|");
	}

	static String unescape(String str) {
		return str.replace("\\|", "|");
	}

	static void setContext(BContextImpl context) {
		ButtonId.context = context;
	}
}
