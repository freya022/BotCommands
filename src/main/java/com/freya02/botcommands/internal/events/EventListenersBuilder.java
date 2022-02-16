package com.freya02.botcommands.internal.events;

import com.freya02.botcommands.api.ExceptionHandler;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.ExecutableInteractionInfo;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.runner.MethodRunner;
import com.freya02.botcommands.internal.utils.EventUtils;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

// We need to avoid using the ClassWalker from ListenerAdapter
// For this, we have to store all the possible triggered consumers by a specific event type
// So we need to get *all* the implementors of an abstract class
public class EventListenersBuilder {
	private static final Logger LOGGER = Logging.getLogger();
	private static final MethodHandles.Lookup lookup = MethodHandles.lookup(); //don't use publicLookup

	//You might notice that there are fewer entries in the Event.class key's value than there is events, that's because some events are interfaces, and interfaces can't extend objects
	// So only the implementors of GenericEvent is the correct count, but we use an Event
	private final Map<Class<?>, List<Class<?>>> implementors = new HashMap<>();
	private final Map<Class<?>, List<EventConsumer>> eventListenersMap = new HashMap<>();
	private final BContextImpl context;

	public EventListenersBuilder(BContextImpl context) {
		this.context = context;

		implementors.putAll(getAllEventImplementors());
	}

	@NotNull
	private static Map<Class<?>, List<Class<?>>> getAllEventImplementors() {
		//You might notice that there are fewer entries in the Event.class key's value than there is events, that's because some events are interfaces, and interfaces can't extend objects
		// So only the implementors of GenericEvent is the correct count, but we use an Event
		Map<Class<?>, List<Class<?>>> implementors = new HashMap<>();

		//Get all override-able methods from ListenerAdapter
		for (Method method : ListenerAdapter.class.getDeclaredMethods()) {
			if (!Modifier.isPublic(method.getModifiers())) continue;

			if (Modifier.isFinal(method.getModifiers())) continue;
			if (Modifier.isAbstract(method.getModifiers())) continue;

			if (method.getParameterCount() != 1) continue;

			final Class<?> eventType = method.getParameterTypes()[0];
			if (eventType.getSimpleName().endsWith("Event")) {
				final Set<Class<?>> subtypes = new HashSet<>(); //We then take all the superclasses of the event

				populateEventSubtypes(subtypes, eventType);

				for (Class<?> subtype : subtypes) { //And bind the superclass to the implementor
					implementors.computeIfAbsent(subtype, x -> new ArrayList<>()).add(eventType);
				}
			}
		}

		return implementors;
	}

	private static void populateEventSubtypes(Set<Class<?>> subtypesList, Class<?> eventType) {
		if (!Event.class.isAssignableFrom(eventType)) return;

		subtypesList.add(eventType);

		populateEventSubtypes(subtypesList, eventType.getSuperclass());

		for (Class<?> superInterface : eventType.getInterfaces()) {
			populateEventSubtypes(subtypesList, superInterface);
		}
	}

	@SuppressWarnings("unchecked")
	public void processEventListener(Object eventListener, Method method) {
		try {
			if (!ReflectionUtils.hasFirstParameter(method, Event.class))
				throw new IllegalArgumentException("Event listener at " + Utils.formatMethodShort(method) + " must have a valid (extends Event) JDA event as first parameter");

			final Class<?> eventType = method.getParameterTypes()[0];

			//Check if the event is listen-able
			EventUtils.checkEvent(context.getJDA(), context.getJDA().getGatewayIntents(), (Class<? extends GenericEvent>) eventType);

			if (method.getParameterCount() > 1) {
				final EventConsumer consumer = getParametrizedEventListener(eventListener, method);

				for (Class<?> eventSubtype : implementors.get(eventType)) {
					getEventConsumers(eventSubtype).add(consumer);
				}

				LOGGER.debug("Added a parametrized {} listener for method {}", eventType.getSimpleName(), Utils.formatMethodShort(method));
			} else {
				final EventConsumer consumer = getFastEventListener(eventListener, method, eventType);

				for (Class<?> eventSubtype : implementors.get(eventType)) {
					getEventConsumers(eventSubtype).add(consumer);
				}

				LOGGER.debug("Added a fast {} listener for method {}", eventType.getSimpleName(), Utils.formatMethodShort(method));
			}
		} catch (Exception e) {
			throw new RuntimeException("An exception occurred while processing an event listener at " + Utils.formatMethodShort(method), e);
		}
	}

	@NotNull
	private List<EventConsumer> getEventConsumers(Class<?> eventType) {
		return eventListenersMap.computeIfAbsent(eventType, x -> new ArrayList<>());
	}

	@NotNull
	private EventConsumer getParametrizedEventListener(Object eventListener, Method method) {
		final MethodParameters<EventListenerParameter> parameters = MethodParameters.of(context, method, EventListenerParameter::new);

		final MethodRunner methodRunner = context.getMethodRunnerFactory().make(eventListener, method);
		final var executableInteractionInfo = new EventListenerExecutableInteractionInfo(method, methodRunner, parameters, eventListener);

		return event -> {
			List<Object> objects = new ArrayList<>(parameters.size() + 1);

			objects.add(event);

			for (final EventListenerParameter parameter : parameters) {
				final Object obj = parameter.getCustomResolver().resolve(context, executableInteractionInfo, event);

				//For some reason using an array list instead of a regular array
				// magically unboxes primitives when passed to Method#invoke
				objects.add(obj);
			}

			methodRunner.invoke(objects.toArray(), getThrowableConsumer(context, event));
		};
	}

	public static Consumer<Throwable> getThrowableConsumer(BContextImpl context, Event event) {
		return e -> {
			final ExceptionHandler handler = context.getUncaughtExceptionHandler();
			if (handler != null) {
				handler.onException(context, event, e);

				return;
			}

			Throwable baseEx = Utils.getException(e);

			Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing an event", baseEx);

			context.dispatchException("Exception in component callback", baseEx);
		};
	}

	@NotNull
	private EventConsumer getFastEventListener(Object eventListener, Method method, Class<?> eventType) {
		String methodName = method.getName();

		try {
			//Good reference: https://stackoverflow.com/a/69078452
			//Some reference: https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/

			final MethodHandle handle = lookup.findVirtual(eventListener.getClass(), methodName, MethodType.methodType(void.class, eventType));
			CallSite site = LambdaMetafactory.metafactory(lookup,
					"accept", //Name of the FI of EventConsumer
					MethodType.methodType(EventConsumer.class, eventListener.getClass()), //MethodType of the type to be transformed into an EventConsumer
					MethodType.methodType(void.class, Event.class), //Base signature of the target method
					handle, //Handle to the target method
					MethodType.methodType(void.class, eventType)); //Exact signature of the target method

			MethodHandle factory = site.getTarget();

			return (EventConsumer) factory.invoke(eventListener);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to generate an EventConsumer for handler method name '" + methodName + "' and type " + eventType, e);
		}
	}

	public void postProcess() {
		context.addEventListeners(new EventListenerImpl(context, eventListenersMap));
	}

	private record EventListenerExecutableInteractionInfo(Method method,
	                                                      MethodRunner methodRunner,
	                                                      MethodParameters<EventListenerParameter> parameters,
	                                                      Object eventListener) implements ExecutableInteractionInfo {

		@Override
		@NotNull
		public Method getMethod() {
			return method;
		}

		@Override
		@NotNull
		public MethodRunner getMethodRunner() {
			return methodRunner;
		}

		@Override
		@NotNull
		public MethodParameters<? extends CommandParameter<?>> getParameters() {
			return parameters;
		}

		@Override
		@NotNull
		public Object getInstance() {
			return eventListener;
		}
	}
}
