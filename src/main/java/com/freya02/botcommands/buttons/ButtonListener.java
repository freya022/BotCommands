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

import static com.freya02.botcommands.buttons.ButtonsBuilder.buttonsMap;

public class ButtonListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
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
			final Cipher decryptCipher = Cipher.getInstance("AES/CTR/NoPadding");

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
