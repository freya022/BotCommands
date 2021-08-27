package com.freya02.botcommands.components.internal;

import com.freya02.botcommands.components.ComponentListener;
import com.freya02.botcommands.components.annotation.JdaButtonListener;
import com.freya02.botcommands.components.annotation.JdaSelectionMenuListener;
import com.freya02.botcommands.components.event.ButtonEvent;
import com.freya02.botcommands.components.event.SelectionEvent;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.ClassInstancer;
import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.parameters.ComponentParameterResolver;
import com.freya02.botcommands.parameters.ParameterResolver;
import com.freya02.botcommands.parameters.ParameterResolvers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
			JdaButtonListener jdaButtonListener = method.getAnnotation(JdaButtonListener.class);
			if (jdaButtonListener != null) {
				handleComponentListener(method, jdaButtonListener.name(), buttonsMap, ButtonEvent.class, "Button listener");

				continue;
			}

			JdaSelectionMenuListener jdaSelectionMenuListener = method.getAnnotation(JdaSelectionMenuListener.class);
			if (jdaSelectionMenuListener != null) {
				handleComponentListener(method, jdaSelectionMenuListener.name(), selectionMenuMap, SelectionEvent.class, "Selection menu");
			}
		}
	}

	private void handleComponentListener(Method method, String handlerName, Map<String, ComponentDescriptor> map, Class<?> firstRequiredArg, String componentType) {
		if (!Utils.hasFirstParameter(method, firstRequiredArg))
			throw new IllegalArgumentException("First parameter of method " + Utils.formatMethodShort(method) + " should be a " + firstRequiredArg.getSimpleName());

		try {
			final Object obj = ClassInstancer.getMethodTarget(context, method);

			if (!method.canAccess(obj))
				throw new IllegalStateException(componentType + " " + Utils.formatMethodShort(method) + " is not public");

			final List<ComponentParameterResolver> resolvers = new ArrayList<>();
			Parameter[] parameters = method.getParameters();
			for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
				Parameter parameter = parameters[i];
				final Class<?> type = Utils.getBoxedType(parameter.getType());

				final ParameterResolver resolver = ParameterResolvers.of(type);

				if (!(resolver instanceof ComponentParameterResolver))
					throw new IllegalArgumentException("Unknown component value type: " + type.getName());

				resolvers.add((ComponentParameterResolver) resolver);
			}

			final ComponentDescriptor newDescriptor = new ComponentDescriptor(obj, method, resolvers);
			final ComponentDescriptor oldVal = map.put(handlerName, newDescriptor);
			if (oldVal != null) {
				throw new IllegalStateException(componentType + " with name " + handlerName + " in " + Utils.formatMethodShort(method) + " was already registered as " + oldVal.getMethod());
			}

			context.getRegistrationListeners().forEach(l -> l.onComponentRegistered(newDescriptor));
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occurred while instantiating the class of the " + componentType + "'s method " + Utils.formatMethodShort(method), e);
		}
	}

	public void postProcess() {
		context.addEventListeners(new ComponentListener(context, buttonsMap, selectionMenuMap));
	}
}
