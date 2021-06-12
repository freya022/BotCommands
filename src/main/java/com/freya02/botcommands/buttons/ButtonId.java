package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.buttons.annotation.JdaButtonListener;
import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.StringJoiner;

import static com.freya02.botcommands.buttons.ButtonsBuilder.buttonsMap;

public class ButtonId {
	private static BContextImpl context;

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}
	 *
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args  The objects objects to use in the string
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String of(String handlerName, Object... args) {
		return create(handlerName, false, 0L, args);
	}

	public static String uniqueOf(String handlerName, Object... args) {
		return create(handlerName, true, 0L, args);
	}

	public static String ofUser(String handlerName, long callerId, Object... args) {
		return create(handlerName, false, callerId, args);
	}

	public static String uniqueOfUser(String handlerName, long callerId, Object... args) {
		return create(handlerName, true, callerId, args);
	}

	private static String create(String handlerName, boolean oneUse, long callerId, Object... args) {
		if (!buttonsMap.containsKey(handlerName))
			throw new IllegalStateException("Button listener with name '" + handlerName + "' doesn't exist");

		final String constructedId = constructId(handlerName, oneUse, callerId, args);

		return Objects.requireNonNull(context.getIdManager(), "ID Manager should be set in order to use Discord components").newId(constructedId);
	}

	@Nonnull
	static String constructId(String handlerName, boolean oneUse, long callerId, Object[] args) {
		StringJoiner idBuilder = new StringJoiner("|")
				.add(escape(handlerName))
				.add(oneUse ? "1" : "0")
				.add(String.valueOf(callerId));
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
