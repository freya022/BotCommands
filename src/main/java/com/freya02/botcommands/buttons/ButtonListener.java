package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import static com.freya02.botcommands.buttons.ButtonId.unescape;
import static com.freya02.botcommands.buttons.ButtonsBuilder.buttonsMap;

public class ButtonListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private static final Pattern SPLIT_PATTERN = Pattern.compile("(?<!\\\\)\\|");
	private static BContextImpl context;

	static void init(BContextImpl context) {
		ButtonListener.context = context;
		ButtonId.setContext(context);

		context.getJDA().addEventListener(new ButtonListener());
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
			final Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			decryptCipher.init(Cipher.DECRYPT_MODE, key.getKey(), key.getIv());

			final byte[] bytes = id.getBytes(StandardCharsets.UTF_8);
			final String decryptedId = new String(decryptCipher.doFinal(Base64.getDecoder().decode(bytes)));
			LOGGER.trace("Received button ID {}", decryptedId);

			String[] args = SPLIT_PATTERN.split(decryptedId);
			final ButtonDescriptor descriptor = buttonsMap.get(unescape(args[0]));

			if (descriptor == null) {
				LOGGER.error("Received a button listener named {} but is not present in the map, listener names: {}", args[0], buttonsMap.keySet());
				return;
			}

			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			final List<Object> methodArgs = new ArrayList<>(descriptor.getResolvers().size() + 1);

			methodArgs.add(event);
			for (int i = 1, splitLength = args.length; i < splitLength; i++) {
				String arg = unescape(args[i]);

				final Object obj = descriptor.getResolvers().get(i - 1).resolve(event, arg);
				if (obj == null) {
					LOGGER.warn("Invalid button id '{}', tried to resolve '{}' but result is null", decryptedId, arg);

					return;
				}

				methodArgs.add(obj);
			}

			descriptor.getMethod().invoke(descriptor.getInstance(), methodArgs.toArray());
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | IllegalAccessException | InvocationTargetException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("An exception occurred while decrypting a button ID", e);
			} else {
				e.printStackTrace();
			}

			context.dispatchException("An exception occurred while decrypting a button ID", e);
		} catch (BadPaddingException e) {
			var exception = new RuntimeException(String.format("Received a BadPaddingException while decrypting a button id from %s (%s) in channel %s (%s)", event.getUser().getAsTag(), event.getUser().getId(), event.getChannel().getName(), event.getChannel().getId()));
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("An exception occurred while decrypting a button ID", exception);
			} else {
				e.printStackTrace();
			}

			context.dispatchException("An exception occurred while decrypting a button ID", exception);
		}
	}
}
