package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.Utils;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static com.freya02.botcommands.buttons.ButtonId.unescape;
import static com.freya02.botcommands.buttons.ButtonsBuilder.buttonsMap;

public class ButtonListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private static final Pattern SPLIT_PATTERN = Pattern.compile("(?<!\\\\)\\|");
	private static BContextImpl context;

	private int buttonThreadNumber = 0;
	private final ExecutorService commandService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in a button listener thread '" + t.getName() + "':", e));
		thread.setName("Button lister thread #" + buttonThreadNumber++);

		return thread;
	});

	static void init(BContextImpl context) {
		ButtonListener.context = context;
		ButtonId.setContext(context);
		ButtonIdFactory.setContext(context);

		context.addEventListeners(new ButtonListener());
	}

	@Override
	public void onButtonClick(@Nonnull ButtonClickEvent event) {
		commandService.submit(() -> {
			try {
				final String id = event.getComponentId();

				final IdManager idManager = context.getIdManager();
				if (idManager == null) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("ID Manager should be set to use Discord components");
					} else {
						System.err.println("ID Manager should be set to use Discord components");
					}

					return;
				}

				final String componentId = idManager.getContent(id);
				if (componentId == null) {
					event.reply("This button is not associated with an action (anymore)")
							.setEphemeral(true)
							.queue();

					return;
				}

				LOGGER.trace("Received button ID {}", componentId);

				String[] args = SPLIT_PATTERN.split(componentId);
				final ButtonDescriptor descriptor = buttonsMap.get(unescape(args[0]));

				if (descriptor == null) {
					LOGGER.error("Received a button listener named {} but is not present in the map, listener names: {}", args[0], buttonsMap.keySet());
					return;
				}

				final String callerId = args[2];
				if (!callerId.equals("0")) {
					if (!event.getUser().getId().equals(callerId)) {
						return;
					}
				}

				final String oneUse = args[1];
				if (oneUse.equals("1")) {
					idManager.removeId(id);
				}

				if (descriptor.getResolvers().size() != args.length - 3) {
					event.reply("This button has invalid content")
							.setEphemeral(true)
							.queue();

					LOGGER.warn("Expected {} arguments, but button with ID '{}' had {} arguments.", componentId, descriptor.getResolvers().size(), args.length - 3);

					return;
				}

				//For some reason using an array list instead of a regular array
				// magically unboxes primitives when passed to Method#invoke
				final List<Object> methodArgs = new ArrayList<>(descriptor.getResolvers().size() + 1);

				methodArgs.add(event);
				for (int i = 3, splitLength = args.length; i < splitLength; i++) {
					String arg = unescape(args[i]);

					final Object obj = descriptor.getResolvers().get(i - 3).resolve(event, arg);
					if (obj == null) {
						LOGGER.warn("Invalid button id '{}', tried to resolve '{}' but result is null", componentId, arg);

						return;
					}

					methodArgs.add(obj);
				}

				descriptor.getMethod().invoke(descriptor.getInstance(), methodArgs.toArray());
			} catch (Exception e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("An exception occurred while processing a button ID from {}", event.getUser().getAsTag(), e);
				} else {
					e.printStackTrace();
				}

				context.dispatchException("An exception occurred while processing a button ID from " + event.getUser().getAsTag(), e);
			}
		});
	}
}
