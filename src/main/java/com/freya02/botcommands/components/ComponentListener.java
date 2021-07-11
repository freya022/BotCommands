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
import java.util.function.Supplier;

public class ComponentListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContext context;
	private final ComponentManager idManager;
	private final Map<String, ComponentDescriptor> buttonsMap;
	private final Map<String, ComponentDescriptor> selectionMenuMap;

	public ComponentListener(BContext context, Map<String, ComponentDescriptor> buttonsMap, Map<String, ComponentDescriptor> selectionMenuMap) {
		this.context = context;
		this.idManager = Utils.getComponentManager(context);
		this.buttonsMap = buttonsMap;
		this.selectionMenuMap = selectionMenuMap;

		Components.setContext(context);
	}

	@Override
	public void onGenericComponentInteractionCreate(@Nonnull GenericComponentInteractionCreateEvent event) {
		if (!(event instanceof ButtonClickEvent) && !(event instanceof SelectionMenuEvent)) return;

		final ComponentType idType = idManager.getIdType(event.getComponentId());

		if (idType == null) {
			event.reply("This component is not usable anymore")
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
				idManager.handlePersistentButton(event,
						e -> onError(event, e.getReason()),
						data -> handlePersistentComponent(event,
								buttonsMap,
								data.getHandlerName(),
								data.getArgs(),
								() -> new ButtonEvent(context, (ButtonClickEvent) event)));

				break;
			case LAMBDA_BUTTON:
				idManager.handleLambdaButton(event,
						e -> onError(event, e.getReason()),
						data -> data.getConsumer().accept(new ButtonEvent(context, (ButtonClickEvent) event))
				);

				break;
			case PERSISTENT_SELECTION_MENU:
				idManager.handlePersistentSelectionMenu(event,
						e -> onError(event, e.getReason()),
						data -> handlePersistentComponent(event,
								selectionMenuMap,
								data.getHandlerName(),
								data.getArgs(),
								() -> new SelectionEvent(context, (SelectionMenuEvent) event)));

				break;
			case LAMBDA_SELECTION_MENU:
				idManager.handleLambdaSelectionMenu(event,
						e -> onError(event, e.getReason()),
						data -> data.getConsumer().accept(new SelectionEvent(context, (SelectionMenuEvent) event)));

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
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("An exception occurred while handling a persistent component '{}' with args {}", handlerName, Arrays.toString(args), e);
			} else {
				System.err.printf("An exception occurred while handling a persistent component '%s' with args %s%n", handlerName, Arrays.toString(args));

				e.printStackTrace();
			}
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
