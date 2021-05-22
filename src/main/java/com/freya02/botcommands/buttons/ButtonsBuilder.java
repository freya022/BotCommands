package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.buttons.annotation.JdaButtonListener;
import com.freya02.botcommands.parameters.ParameterResolver;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonsBuilder {
	private static final Map<String, ButtonDescriptor> map = new HashMap<>();
	private final BContextImpl context;

	public ButtonsBuilder(BContextImpl context) {
		this.context = context;
	}

	public void processButtonListener(Class<?> clazz) {
		for (Method method : clazz.getDeclaredMethods()) {
			JdaButtonListener annotation = method.getAnnotation(JdaButtonListener.class);
			if (annotation != null) {
				if (!ButtonClickEvent.class.isAssignableFrom(method.getParameterTypes()[0]))
					throw new IllegalArgumentException("First parameter of method " + method + " should be a ButtonClickEvent");

				final Object obj;
				if (Modifier.isStatic(method.getModifiers())) {
					obj = null;
				} else {
					final Object instance = context.getClassToObjMap().get(clazz);

					if (instance == null) {
						try {
							final Constructor<?> constructor = clazz.getConstructor();
							if (!constructor.canAccess(null))
								throw new IllegalStateException("Class constructor for a button listener " + constructor + " is not public");

							obj = constructor.newInstance();

							context.getClassToObjMap().put(clazz, obj); //Need to reuse object for next listeners
						} catch (NoSuchMethodException ignored) {
							throw new RuntimeException("Button listener " + method + " is an instance method but no instance of it has been registered and is not default constructible, this can be solved by moving it with a (slash/prefixed) command");
						} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
							throw new RuntimeException("Button listener " + method + " wasn't able to be constructed", e);
						}
					} else {
						obj = instance;
					}
				}

				if (!method.canAccess(obj))
					throw new IllegalStateException("Button listener " + method + " is not public");

				final List<ParameterResolver> resolvers = new ArrayList<>();
				Parameter[] parameters = method.getParameters();
				for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
					Parameter parameter = parameters[i];
					final Class<?> type = parameter.getType();

					final ParameterResolver resolver = ParameterResolver.of(type);

					if (resolver == null || !resolver.isButtonSupported())
						throw new IllegalArgumentException("Unknown button value type: " + type.getName());

					resolvers.add(resolver);
				}

				map.put(annotation.name(), new ButtonDescriptor(obj, method, resolvers));
			}
		}
	}

	public void postProcess() {
		ButtonListener.init(context, map);
	}
}
