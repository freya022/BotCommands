package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
import java.util.Map;

public class ButtonListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private static BContextImpl context;
	private static Map<String, ButtonDescriptor> map;

	static void init(BContextImpl context, Map<String, ButtonDescriptor> map) {
		ButtonListener.context = context;
		ButtonListener.map = map;

		context.getJDA().addEventListener(new ButtonListener());
	}

	public static String getButtonId(Guild guild, Object... args) {
		return encryptId(context.getKeyProvider().getKey(guild), args);
	}

	public static String getButtonId(User user, Object... args) {
		return encryptId(context.getKeyProvider().getKey(user), args);
	}

	public static String getButtonId(SlashCommandEvent event, Object... args) {
		if (event.isFromGuild()) {
			return encryptId(context.getKeyProvider().getKey(event.getGuild()), args);
		} else {
			return encryptId(context.getKeyProvider().getKey(event.getUser()), args);
		}
	}

	@Nonnull
	private static String encryptId(Key key, Object[] args) {
		final byte[] idBytes = getIdBytes(args);

		try {
			final Cipher encryptCipher = Cipher.getInstance("AES/CTR/PKCS5Padding");

			encryptCipher.init(Cipher.ENCRYPT_MODE, key.getKey(), key.getIv());

			final String encodedId = new String(encryptCipher.doFinal(idBytes));
			if (encodedId.length() > 100)
				throw new IllegalArgumentException("Encrypted id should not be larger than 100 bytes, consider having less info in your arguments");

			return encodedId;
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	private static byte[] getIdBytes(Object[] args) {
		StringBuilder idBuilder = new StringBuilder(100);
		for (int i = 0, argsLength = args.length; i < argsLength; i++) {
			Object arg = args[i];

			final String s = arg.toString();

			if (s.contains("²")) throw new IllegalArgumentException("Argument #" + i + " cannot have a ² inside");

			idBuilder.append(s).append('²');
		}

		return idBuilder.toString().getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void onButtonClick(@Nonnull ButtonClickEvent event) {
		final Key key;
		if (event.isFromGuild()) {
			key = context.getKeyProvider().getKey(event.getGuild());
		} else {
			key = context.getKeyProvider().getKey(event.getUser());
		}

		final String id = event.getButton().getId();

		try {
			final Cipher decryptCipher = Cipher.getInstance("AES/CTR/PKCS5Padding");

			decryptCipher.init(Cipher.DECRYPT_MODE, key.getKey(), key.getIv());

			final String decryptedId = new String(decryptCipher.doFinal(id.getBytes(StandardCharsets.UTF_8)));

			String[] args = decryptedId.split("²");
			final ButtonDescriptor descriptor = map.get(args[0]);

			final Object[] methodArgs = new Object[descriptor.getResolvers().size() + 1];
			methodArgs[0] = event;

			for (int i = 1, splitLength = args.length; i < splitLength; i++) {
				String arg = args[i];

				final Object obj = descriptor.getResolvers().get(i).resolve(event, arg);

				methodArgs[i - 1] = obj;
			}

			descriptor.getMethod().invoke(descriptor.getInstance(), methodArgs);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | IllegalAccessException | InvocationTargetException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("An exception occurred while decrypting a button id", e);
			} else {
				e.printStackTrace();
			}
		}
	}
}
