package com.freya02.botcommands.application.context.message;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.application.ApplicationCommand;
import com.freya02.botcommands.application.ApplicationCommandInfo;
import com.freya02.botcommands.application.context.ContextCommandParameter;
import com.freya02.botcommands.application.context.annotations.JdaMessageCommand;
import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.parameters.MessageContextParameterResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.commands.MessageContextCommandEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MessageCommandInfo extends ApplicationCommandInfo {
	private final Object instance;
	private final ContextCommandParameter<MessageContextParameterResolver>[] commandParameters;

	@SuppressWarnings("unchecked")
	public MessageCommandInfo(ApplicationCommand instance, Method method) {
		super(instance, method.getAnnotation(JdaMessageCommand.class),
				method,
				method.getAnnotation(JdaMessageCommand.class).name());

		this.instance = instance;
		this.commandParameters = new ContextCommandParameter[commandMethod.getParameterCount() - 1];

		final Class<?>[] parameterTypes = method.getParameterTypes();
		
		if (!GlobalMessageEvent.class.isAssignableFrom(parameterTypes[0])) {
			throw new IllegalArgumentException("First argument should be a GlobalUserEvent for method " + Utils.formatMethodShort(method));
		}

		for (int i = 1, parametersLength = commandMethod.getParameterCount(); i < parametersLength; i++) {
			final Parameter parameter = commandMethod.getParameters()[i];
			final Class<?> type = parameter.getType();

			if (Member.class.isAssignableFrom(type)) {
				if (!isGuildOnly())
					throw new IllegalArgumentException("The message command " + Utils.formatMethodShort(commandMethod) + " cannot have a " + type.getSimpleName() + " parameter as it is not guild-only");
			}

			commandParameters[i - 1] = new ContextCommandParameter<>(MessageContextParameterResolver.class, type);
		}
	}

	public boolean execute(BContext context, MessageContextCommandEvent event) {
		try {
			final Object[] objects = new Object[commandParameters.length + 1];
			if (guildOnly) {
				objects[0] = new GuildMessageEvent(context, event);
			} else {
				objects[0] = new GlobalMessageEvent(context, event);
			}
			
			for (int i = 0, commandParametersLength = commandParameters.length; i < commandParametersLength; i++) {
				ContextCommandParameter<MessageContextParameterResolver> parameter = commandParameters[i];

				objects[i + 1] = parameter.getResolver().resolve(event);
			}

			commandMethod.invoke(instance, objects);

			return true;
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
