package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.ExceptionHandler;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.components.event.SelectionEvent;
import com.freya02.botcommands.api.parameters.ComponentParameterResolver;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.RunnableEx;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComponentListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();

	private final ExecutorService idHandlingExecutor = Executors.newSingleThreadExecutor(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in a component ID handler thread '" + t.getName() + "':", e));
		thread.setName("Component ID handling thread");

		return thread;
	});

	private final BContextImpl context;
	private final ComponentManager componentManager;

	private final Map<String, ComponentDescriptor> buttonsMap;
	private final Map<String, ComponentDescriptor> selectionMenuMap;

	private int callbackThreadNumber;
	private final ExecutorService callbackExecutor = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in a component callback thread '" + t.getName() + "':", e));
		thread.setName("Component callback thread #" + callbackThreadNumber++);

		return thread;
	});

	public ComponentListener(BContextImpl context, Map<String, ComponentDescriptor> buttonsMap, Map<String, ComponentDescriptor> selectionMenuMap) {
		this.context = context;
		this.componentManager = Utils.getComponentManager(context);
		this.buttonsMap = buttonsMap;
		this.selectionMenuMap = selectionMenuMap;

		Components.setContext(context);
	}

	@SubscribeEvent
	@Override
	public void onGenericComponentInteractionCreate(@NotNull GenericComponentInteractionCreateEvent event) {
		if (!(event instanceof ButtonInteractionEvent) && !(event instanceof SelectMenuInteractionEvent)) return;

		runHandler(() -> handleComponentInteraction(event), event);
	}

	private void handleComponentInteraction(@NotNull GenericComponentInteractionCreateEvent event) throws Exception {
		for (ComponentInteractionFilter componentFilter : context.getComponentFilters()) {
			if (!componentFilter.isAccepted(new ComponentFilteringData(context, event))) {
				LOGGER.trace("Cancelled component interaction due to filter");

				return;
			}
		}

		try (FetchResult fetchResult = componentManager.fetchComponent(event.getComponentId())) {
			final FetchedComponent fetchedComponent = fetchResult.getFetchedComponent();

			if (fetchedComponent == null) {
				event.reply(context.getDefaultMessages(event).getComponentNotFoundErrorMsg())
						.setEphemeral(true)
						.queue();

				return;
			}

			final ComponentType idType = fetchedComponent.getType();
			if ((idType == ComponentType.PERSISTENT_BUTTON || idType == ComponentType.LAMBDA_BUTTON) && !(event instanceof ButtonInteractionEvent)) {
				LOGGER.error("Received a button id type but event is not a ButtonInteractionEvent");

				return;
			}

			if ((idType == ComponentType.PERSISTENT_SELECTION_MENU || idType == ComponentType.LAMBDA_SELECTION_MENU) && !(event instanceof SelectMenuInteractionEvent)) {
				LOGGER.error("Received a selection menu id type but event is not a SelectMenuInteractionEvent");

				return;
			}

			switch (idType) {
				case PERSISTENT_BUTTON -> componentManager.handlePersistentButton(event,
						fetchResult,
						e -> onError(event, e),
						data -> runCallback(() -> handlePersistentComponent(event,
										buttonsMap,
										data.getHandlerName(),
										data.getArgs(),
										descriptor -> new ButtonEvent(descriptor.getMethod(), context, (ButtonInteractionEvent) event)),
								event));
				case LAMBDA_BUTTON -> componentManager.handleLambdaButton(event,
						fetchResult,
						e -> onError(event, e),
						data -> runCallback(() -> data.getConsumer().accept(new ButtonEvent(null, context, (ButtonInteractionEvent) event)), event)
				);
				case PERSISTENT_SELECTION_MENU -> componentManager.handlePersistentSelectMenu(event,
						fetchResult,
						e -> onError(event, e),
						data -> runCallback(() -> handlePersistentComponent(event,
										selectionMenuMap,
										data.getHandlerName(),
										data.getArgs(),
										descriptor -> new SelectionEvent(descriptor.getMethod(), context, (SelectMenuInteractionEvent) event)),
								event));
				case LAMBDA_SELECTION_MENU -> componentManager.handleLambdaSelectMenu(event,
						fetchResult,
						e -> onError(event, e),
						data -> runCallback(() -> data.getConsumer().accept(new SelectionEvent(null, context, (SelectMenuInteractionEvent) event)), event));
				default -> throw new IllegalArgumentException("Unknown id type: " + idType.name());
			}
		}
	}

	private void runHandler(RunnableEx code, @NotNull GenericComponentInteractionCreateEvent event) {
		idHandlingExecutor.execute(() -> {
			try {
				long start = System.nanoTime();
				code.run();
				long end = System.nanoTime();

				LOGGER.trace("Component handler took {} ms", (end - start) / 1000000.0);
			} catch (Throwable e) {
				final ExceptionHandler handler = context.getUncaughtExceptionHandler();
				if (handler != null) {
					handler.onException(context, event, e);

					return;
				}

				Throwable baseEx = Utils.getException(e);

				Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing the component ID handler", baseEx);
				if (event.isAcknowledged()) {
					event.getHook().sendMessage(context.getDefaultMessages(event).getGeneralErrorMsg()).setEphemeral(true).queue();
				} else {
					event.reply(context.getDefaultMessages(event).getGeneralErrorMsg()).setEphemeral(true).queue();
				}

				context.dispatchException("Exception in component ID handler", baseEx);
			}
		});
	}

	private void runCallback(RunnableEx code, @NotNull GenericComponentInteractionCreateEvent event) {
		callbackExecutor.execute(() -> {
			try {
				long start = System.nanoTime();
				code.run();
				long end = System.nanoTime();

				LOGGER.trace("Component callback took {} ms", (end - start) / 1000000.0);
			} catch (Throwable e) {
				final ExceptionHandler handler = context.getUncaughtExceptionHandler();
				if (handler != null) {
					handler.onException(context, event, e);

					return;
				}

				Throwable baseEx = Utils.getException(e);

				Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing a component callback", baseEx);
				if (event.isAcknowledged()) {
					event.getHook().sendMessage(context.getDefaultMessages(event).getGeneralErrorMsg()).setEphemeral(true).queue();
				} else {
					event.reply(context.getDefaultMessages(event).getGeneralErrorMsg()).setEphemeral(true).queue();
				}

				context.dispatchException("Exception in component callback", baseEx);
			}
		});
	}

	private void handlePersistentComponent(GenericComponentInteractionCreateEvent event,
	                                       Map<String, ComponentDescriptor> map,
	                                       String handlerName,
	                                       String[] args,
	                                       Function<ComponentDescriptor, ? extends GenericComponentInteractionCreateEvent> eventFunction) {
		final ComponentDescriptor descriptor = map.get(handlerName);

		if (descriptor == null) {
			LOGGER.error("No component descriptor found for component handler '{}'", handlerName);

			return;
		}

		final var parameters = descriptor.getParameters();
		if (parameters.getOptionCount() != args.length) {
			throw new IllegalArgumentException("Resolver for %s has %d arguments but component had %d data objects".formatted(Utils.formatMethodShort(descriptor.getMethod()), parameters.size(), args.length));
		}

		final Consumer<Throwable> throwableConsumer = getThrowableConsumer(handlerName, args);
		try {
			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			final List<Object> methodArgs = new ArrayList<>(parameters.size() + 1);

			methodArgs.add(eventFunction.apply(descriptor));

			int optionIndex = 0;
			for (final CommandParameter<ComponentParameterResolver> parameter : parameters) {
				final Object obj;
				if (parameter.isOption()) {
					final String arg = args[optionIndex];
					optionIndex++;

					obj = parameter.getResolver().resolve(context, descriptor, event, arg);

					if (obj == null) {
						throw new IllegalArgumentException("Component id '%s', tried to resolve '%s' with an option resolver %s on method %s but result is null".formatted(
								event.getComponentId(),
								arg,
								parameter.getCustomResolver().getClass().getSimpleName(),
								Utils.formatMethodShort(descriptor.getMethod())
						));
					}
				} else {
					obj = parameter.getCustomResolver().resolve(context, descriptor, event);

					if (obj == null) {
						throw new IllegalArgumentException("Component id '%s', tried to use custom resolver %s on method %s but result is null".formatted(
								event.getComponentId(),
								parameter.getCustomResolver().getClass().getSimpleName(),
								Utils.formatMethodShort(descriptor.getMethod())
						));
					}
				}

				methodArgs.add(obj);
			}

			descriptor.getMethodRunner().invoke(methodArgs.toArray(), throwableConsumer);
		} catch (Exception e) {
			throwableConsumer.accept(e);
		}
	}

	@NotNull
	private Consumer<Throwable> getThrowableConsumer(String handlerName, String[] args) {
		return e -> LOGGER.error("An exception occurred while handling a persistent component '{}' with args {}", handlerName, Arrays.toString(args), e);
	}

	private void onError(GenericComponentInteractionCreateEvent event, ComponentErrorReason reason) {
		event.reply(reason.getReason(context.getDefaultMessages(event)))
				.setEphemeral(true)
				.queue();
	}
}
