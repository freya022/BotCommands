package com.freya02.botcommands.application.context.user;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.application.ApplicationCommandInfo;
import com.freya02.botcommands.application.context.ContextCommandParameter;
import com.freya02.botcommands.application.context.annotations.JdaUserCommand;
import com.freya02.botcommands.application.slash.ApplicationCommand;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import com.freya02.botcommands.parameters.UserContextParameterResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.commands.UserContextCommandEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class UserCommandInfo extends ApplicationCommandInfo {
	private final Object instance;
	private final ContextCommandParameter<UserContextParameterResolver>[] commandParameters;

	@SuppressWarnings("unchecked")
	public UserCommandInfo(ApplicationCommand instance, Method method) {
		super(instance, method.getAnnotation(JdaUserCommand.class),
				AnnotationUtils.getAnnotationValue(method.getAnnotation(JdaUserCommand.class), "name"),
				method);

		this.instance = instance;
		this.commandParameters = new ContextCommandParameter[commandMethod.getParameterCount() - 1];

		final Class<?>[] parameterTypes = method.getParameterTypes();
		
		if (!GlobalUserEvent.class.isAssignableFrom(parameterTypes[0])) {
			throw new IllegalArgumentException("First argument should be a GlobalUserEvent for method " + method);
		}

		for (int i = 1, parametersLength = commandMethod.getParameterCount(); i < parametersLength; i++) {
			final Parameter parameter = commandMethod.getParameters()[i];
			final Class<?> type = parameter.getType();

			if (Member.class.isAssignableFrom(type)) {
				if (!isGuildOnly())
					throw new IllegalArgumentException("The user command " + commandMethod + " cannot have a " + type.getSimpleName() + " parameter as it is not guild-only");
			}

			commandParameters[i - 1] = new ContextCommandParameter<>(UserContextParameterResolver.class, type);
		}
	}

	@Override
	public String getPath() {
		return name;
	}

	@Override
	public int getPathComponents() {
		return 1;
	}

	public boolean execute(BContext context, UserContextCommandEvent event) {
		try {
			final Object[] objects = new Object[commandParameters.length + 1];
			if (guildOnly) {
				objects[0] = new GuildUserEvent(context, event);
			} else {
				objects[0] = new GlobalUserEvent(context, event);
			}
			
			for (int i = 0, commandParametersLength = commandParameters.length; i < commandParametersLength; i++) {
				ContextCommandParameter<UserContextParameterResolver> parameter = commandParameters[i];

				objects[i + 1] = parameter.getResolver().resolve(event);
			}

			commandMethod.invoke(instance, objects);

			return true;
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
