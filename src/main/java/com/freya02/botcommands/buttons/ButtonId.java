package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.buttons.annotation.JdaButtonListener;

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
		final ButtonDescriptor descriptor = buttonsMap.get(handlerName);
		if (descriptor == null) throw new IllegalStateException("Button listener with name '" + handlerName + "' doesn't exist");

//		Class<?>[] parameterTypes = descriptor.getMethod().getParameterTypes();
//		for (int i = 1, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
//			final Class<?> parameterType = parameterTypes[i];
//			final Class<?> argType = args[i - 1].getClass();
//
//			if (parameterType.isPrimitive()) {
//				if (argType == Boolean.class && parameterType != boolean.class
//						|| argType == Double.class && parameterType != double.class
//						|| argType == Long.class && parameterType != long.class) {
//					throw new IllegalStateException("Button handler's parameter " + parameterType.getName() + " is not compatible with " + argType.getName());
//				}
//			} else if (!parameterType.isAssignableFrom(argType)) {
//				throw new IllegalStateException("Button handler's parameter " + parameterType.getName() + " is not compatible with " + argType.getName());
//			}
//		}

		final String constructedId = constructId(handlerName, args);

		return Objects.requireNonNull(context.getIdManager(), "ID Manager should be set to use Discord components").newId(constructedId);
	}

	@Nonnull
	private static String constructId(String handlerName, Object[] args) {
		StringJoiner idBuilder = new StringJoiner("|").add(escape(handlerName));
		for (Object arg : args) {
			final String s = escape(arg.toString());

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
