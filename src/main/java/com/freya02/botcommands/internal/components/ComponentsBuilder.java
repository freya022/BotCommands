package com.freya02.botcommands.internal.components;

import com.freya02.botcommands.api.components.ComponentListener;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.components.event.EntitySelectionEvent;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.utils.ClassInstancer;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ComponentsBuilder {
	private final Map<String, ComponentDescriptor> buttonsMap = new HashMap<>();
	private final Map<String, ComponentDescriptor> selectionMenuMap = new HashMap<>();
	private final BContextImpl context;

	public ComponentsBuilder(BContextImpl context) {
		this.context = context;
	}

	public void processClass(Class<?> clazz) {
		for (Method method : clazz.getDeclaredMethods()) {
			JDAButtonListener jdaButtonListener = method.getAnnotation(JDAButtonListener.class);
			if (jdaButtonListener != null) {
				handleComponentListener(method, jdaButtonListener.name(), buttonsMap, ButtonEvent.class, "Button listener");

				continue;
			}

			JDASelectionMenuListener jdaStringSelectMenuListener = method.getAnnotation(JDASelectionMenuListener.class);
			if (jdaStringSelectMenuListener != null) {
				if (ReflectionUtils.hasFirstParameter(method, StringSelectionEvent.class)) {
					handleComponentListener(method, jdaStringSelectMenuListener.name(), selectionMenuMap, StringSelectionEvent.class, "String selection menu");
				} else if (ReflectionUtils.hasFirstParameter(method, EntitySelectionEvent.class)) {
					handleComponentListener(method, jdaStringSelectMenuListener.name(), selectionMenuMap, EntitySelectionEvent.class, "Entity selection menu");
				} else {
					throw new IllegalArgumentException("First parameter of method %s should be either a %s or %s"
							.formatted(Utils.formatMethodShort(method), StringSelectionEvent.class.getSimpleName(), EntitySelectionEvent.class.getSimpleName()));
				}
			}
		}
	}

	private void handleComponentListener(Method method, String handlerName, Map<String, ComponentDescriptor> map, Class<?> firstRequiredArg, String componentType) {
		if (!ReflectionUtils.hasFirstParameter(method, firstRequiredArg))
			throw new IllegalArgumentException("First parameter of method " + Utils.formatMethodShort(method) + " should be a " + firstRequiredArg.getSimpleName());

		try {
			final Object obj = ClassInstancer.getMethodTarget(context, method);

			if (!method.canAccess(obj))
				throw new IllegalStateException(componentType + " " + Utils.formatMethodShort(method) + " is not public");

			final ComponentDescriptor newDescriptor = new ComponentDescriptor(context, obj, method);
			final ComponentDescriptor oldVal = map.put(handlerName, newDescriptor);
			if (oldVal != null) {
				throw new IllegalStateException(componentType + " with name " + handlerName + " in " + Utils.formatMethodShort(method) + " was already registered as " + oldVal.getMethod());
			}

			context.getRegistrationListeners().forEach(l -> l.onComponentRegistered(newDescriptor));
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occurred while instantiating the class of the " + componentType + "'s method " + Utils.formatMethodShort(method), e);
		} catch (Throwable e) {
			throw new RuntimeException("An exception occurred while processing components listener at " + Utils.formatMethodShort(method), e);
		}
	}

	public void postProcess() {
		context.addEventListeners(new ComponentListener(context, buttonsMap, selectionMenuMap));
	}
}
