package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.buttons.annotation.JdaButtonListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;

import static com.freya02.botcommands.buttons.ButtonsBuilder.buttonsMap;

public class ButtonListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private static BContextImpl context;

	static void init(BContextImpl context) {
		ButtonListener.context = context;

		context.getJDA().addEventListener(new ButtonListener());
	}

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}
	 *
	 * @param guild The guild in which the command occurred
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args  The objects objects to use in the string
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String getButtonId(Guild guild, String handlerName, Object... args) {
		final KeyProvider provider = getKeyProvider();

		return encryptId(handlerName, provider.getKey(guild), args);
	}

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}
	 *
	 * @param user The DM user
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args  The objects objects to use in the string
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String getButtonId(User user, String handlerName, Object... args) {
		final KeyProvider provider = getKeyProvider();

		return encryptId(handlerName, provider.getKey(user), args);
	}

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}
	 *
	 * @param event The current slash command event
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args  The objects objects to use in the string
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String getButtonId(SlashCommandEvent event, String handlerName, Object... args) {
		final KeyProvider provider = getKeyProvider();

		if (event.isFromGuild()) {
			return encryptId(handlerName, provider.getKey(event.getGuild()), args);
		} else {
			return encryptId(handlerName, provider.getKey(event.getUser()), args);
		}
	}

	/**
	 * Creates a button ID with the supplied arguments, so they can be passed to a {@linkplain JdaButtonListener button listener}
	 *
	 * @param event The current button command event
	 * @param handlerName Name of the method which should handle the provided arguments
	 * @param args  The objects objects to use in the string
	 * @return The encrypted button ID containing the provided arguments
	 */
	public static String getButtonId(ButtonClickEvent event, String handlerName, Object... args) {
		final KeyProvider provider = getKeyProvider();

		if (event.isFromGuild()) {
			return encryptId(handlerName, provider.getKey(event.getGuild()), args);
		} else {
			return encryptId(handlerName, provider.getKey(event.getUser()), args);
		}
	}

	@NotNull
	private static KeyProvider getKeyProvider() {
		final KeyProvider provider = context.getKeyProvider();
		if (provider == null) throw new IllegalStateException("Attempted to use encrypted buttons but no KeyProvider was set");
		return provider;
	}

	@Nonnull
	private static String encryptId(String handlerName, Key key, Object[] args) {
		final ButtonDescriptor descriptor = buttonsMap.get(handlerName);
		if (descriptor == null) throw new IllegalStateException("Button listener with name '" + handlerName + "' doesn't exist");

		Class<?>[] parameterTypes = descriptor.getMethod().getParameterTypes();
		for (int i = 1, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
			final Class<?> parameterType = parameterTypes[i];
			final Class<?> argType = args[i - 1].getClass();

			if (parameterType.isPrimitive()) {
				if (argType == Boolean.class && parameterType != boolean.class
						|| argType == Double.class && parameterType != double.class
						|| argType == Long.class && parameterType != long.class) {
					throw new IllegalStateException("Button handler's parameter " + parameterType.getName() + " is not compatible with " + argType.getName());
				}
			} else if (!parameterType.isAssignableFrom(argType)) {
				throw new IllegalStateException("Button handler's parameter " + parameterType.getName() + " is not compatible with " + argType.getName());
			}
		}

		final String idBytes = constructId(handlerName, args);

		try {
			final Cipher encryptCipher = Cipher.getInstance("AES/CTR/PKCS5Padding");

			encryptCipher.init(Cipher.ENCRYPT_MODE, key.getKey(), key.getIv());

			final String encodedId = new String(Base64.getEncoder().encode(encryptCipher.doFinal(idBytes.getBytes(StandardCharsets.UTF_8))));
			if (encodedId.length() > 100)
				throw new IllegalArgumentException("Encrypted id should not be larger than 100 bytes, consider having less info in your arguments");

			LOGGER.trace("Sent button id {} for handle {}", idBytes, handlerName);

			return encodedId;
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	private static String constructId(String handlerName, Object[] args) {
		StringJoiner idBuilder = new StringJoiner("²").add(handlerName);
		for (int i = 0, argsLength = args.length; i < argsLength; i++) {
			Object arg = args[i];

			final String s = arg.toString();

			if (s.contains("²")) throw new IllegalArgumentException("Argument #" + i + " cannot have a ² inside");

			idBuilder.add(s);
		}

		return idBuilder.toString();
	}

	@Override
	public void onButtonClick(@Nonnull ButtonClickEvent event) {
		if (context.getKeyProvider() == null) return;

		final Key key;
		if (event.isFromGuild()) {
			key = context.getKeyProvider().getKey(event.getGuild());
		} else {
			key = context.getKeyProvider().getKey(event.getUser());
		}

		final String id = event.getComponentId();

		try {
			final Cipher decryptCipher = Cipher.getInstance("AES/CTR/PKCS5Padding");

			decryptCipher.init(Cipher.DECRYPT_MODE, key.getKey(), key.getIv());

			final byte[] bytes = id.getBytes(StandardCharsets.UTF_8);
			final String decryptedId = new String(decryptCipher.doFinal(Base64.getDecoder().decode(bytes)));
			LOGGER.trace("Received button ID {}", decryptedId);

			String[] args = decryptedId.split("²");
			final ButtonDescriptor descriptor = buttonsMap.get(args[0]);

			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			final List<Object> methodArgs = new ArrayList<>(descriptor.getResolvers().size() + 1);

			methodArgs.add(event);
			for (int i = 1, splitLength = args.length; i < splitLength; i++) {
				String arg = args[i];

				final Object obj = descriptor.getResolvers().get(i - 1).resolve(event, arg);
				if (obj == null) {
					LOGGER.warn("Invalid button id '{}', tried to resolve '{}' but result is null", decryptedId, arg);

					return;
				}

				methodArgs.add(obj);
			}

			descriptor.getMethod().invoke(descriptor.getInstance(), methodArgs.toArray());
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | IllegalAccessException | InvocationTargetException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("An exception occurred while decrypting a button id", e);
			} else {
				e.printStackTrace();
			}
		}
	}
}
