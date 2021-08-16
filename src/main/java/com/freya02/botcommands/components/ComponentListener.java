package com.freya02.botcommands.components;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.components.event.ButtonEvent;
import com.freya02.botcommands.components.event.SelectionEvent;
import com.freya02.botcommands.components.internal.ComponentDescriptor;
import com.freya02.botcommands.parameters.ComponentParameterResolver;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ComponentListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();

	private final ExecutorService idHandlingExecutor = Executors.newSingleThreadExecutor(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in a component ID handler thread '" + t.getName() + "':", e));
		thread.setName("Component ID handling thread");

		return thread;
	});

	private int callbackThreadNumber;
	private final ExecutorService callbackExecutor = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in a component callback thread '" + t.getName() + "':", e));
		thread.setName("Component callback thread #" + callbackThreadNumber++);

		return thread;
	});

	private final BContext context;
	private final ComponentManager componentManager;
	private final Map<String, ComponentDescriptor> buttonsMap;
	private final Map<String, ComponentDescriptor> selectionMenuMap;

	public ComponentListener(BContext context, Map<String, ComponentDescriptor> buttonsMap, Map<String, ComponentDescriptor> selectionMenuMap) {
		this.context = context;
		this.componentManager = Utils.getComponentManager(context);
		this.buttonsMap = buttonsMap;
		this.selectionMenuMap = selectionMenuMap;

		Components.setContext(context);
	}

	@Override
	public void onGenericComponentInteractionCreate(@Nonnull GenericComponentInteractionCreateEvent event) {
		if (!(event instanceof ButtonClickEvent) && !(event instanceof SelectionMenuEvent)) return;

		idHandlingExecutor.submit(() -> handleComponentInteraction(event));
	}

	private void handleComponentInteraction(@Nonnull GenericComponentInteractionCreateEvent event) {
		final ComponentType idType = componentManager.getIdType(event.getComponentId());

		if (idType == null) {
			event.reply(context.getDefaultMessages().getNullComponentTypeErrorMsg())
					.setEphemeral(true)
					.queue();

			return;
		}

		if ((idType == ComponentType.PERSISTENT_BUTTON || idType == ComponentType.LAMBDA_BUTTON) && !(event instanceof ButtonClickEvent)) {
			LOGGER.error("Received a button id type but event is not a ButtonClickEvent");

			return;
		}

		if ((idType == ComponentType.PERSISTENT_SELECTION_MENU || idType == ComponentType.LAMBDA_SELECTION_MENU) && !(event instanceof SelectionMenuEvent)) {
			LOGGER.error("Received a selection menu id type but event is not a SelectionMenuEvent");

			return;
		}

		switch (idType) {
			case PERSISTENT_BUTTON:
				componentManager.handlePersistentButton(event,
						e -> onError(event, e.getReason()),
						data -> callbackExecutor.submit(() -> handlePersistentComponent(event,
								buttonsMap,
								data.getHandlerName(),
								data.getArgs(),
								() -> new ButtonEvent(context, (ButtonClickEvent) event))));

				break;
			case LAMBDA_BUTTON:
				componentManager.handleLambdaButton(event,
						e -> onError(event, e.getReason()),
						data -> callbackExecutor.submit(() -> data.getConsumer().accept(new ButtonEvent(context, (ButtonClickEvent) event)))
				);

				break;
			case PERSISTENT_SELECTION_MENU:
				componentManager.handlePersistentSelectionMenu(event,
						e -> onError(event, e.getReason()),
						data -> callbackExecutor.submit(() -> handlePersistentComponent(event,
								selectionMenuMap,
								data.getHandlerName(),
								data.getArgs(),
								() -> new SelectionEvent(context, (SelectionMenuEvent) event))));

				break;
			case LAMBDA_SELECTION_MENU:
				componentManager.handleLambdaSelectionMenu(event,
						e -> onError(event, e.getReason()),
						data -> callbackExecutor.submit(() -> data.getConsumer().accept(new SelectionEvent(context, (SelectionMenuEvent) event))));

				break;
			default:
				throw new IllegalArgumentException("Unknown id type: " + idType.name());
		}
	}

	private void handlePersistentComponent(GenericComponentInteractionCreateEvent event,
	                                       Map<String, ComponentDescriptor> map,
	                                       String handlerName,
	                                       String[] args,
	                                       Supplier<? extends GenericComponentInteractionCreateEvent> eventFunction) {
		final ComponentDescriptor descriptor = map.get(handlerName);

		if (descriptor == null) {
			LOGGER.error("No component descriptor found for component handler '{}'", handlerName);

			return;
		}

		final List<ComponentParameterResolver> resolvers = descriptor.getResolvers();
		if (resolvers.size() != args.length) {
			LOGGER.warn("Resolver for {} has {} arguments but component had {} data objects", descriptor.getMethod(), resolvers.size(), args);

			onError(event, "Invalid component data");

			return;
		}

		try {
			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			final List<Object> methodArgs = new ArrayList<>(resolvers.size() + 1);

			methodArgs.add(eventFunction.get());
			for (int i = 0, resolversSize = resolvers.size(); i < resolversSize; i++) {
				ComponentParameterResolver resolver = resolvers.get(i);

				final Object obj = resolver.resolve(event, args[i]);
				if (obj == null) {
					LOGGER.warn("Invalid component id '{}', tried to resolve '{}' with a {} but result is null", event.getComponentId(), args[i], resolver.getClass().getSimpleName());

					return;
				}

				methodArgs.add(obj);
			}

			descriptor.getMethod().invoke(descriptor.getInstance(), methodArgs.toArray());
		} catch (Exception e) {
			LOGGER.error("An exception occurred while handling a persistent component '{}' with args {}", handlerName, Arrays.toString(args), e);
		}
	}

	private void onError(GenericComponentInteractionCreateEvent event, String reason) {
		if (reason != null) {
			event.reply(reason)
					.setEphemeral(true)
					.queue();
		} else {
			event.deferEdit().queue();
		}
	}
}
