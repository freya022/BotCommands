package com.freya02.botcommands.application.context.message;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.application.ApplicationCommand;
import com.freya02.botcommands.application.ApplicationCommandInfo;
import com.freya02.botcommands.application.ApplicationCommandParameter;
import com.freya02.botcommands.application.context.ContextCommandParameter;
import com.freya02.botcommands.application.context.annotations.JdaMessageCommand;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.utils.Utils;
import com.freya02.botcommands.parameters.MessageContextParameterResolver;
import net.dv8tion.jda.api.events.interaction.commands.MessageContextCommandEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MessageCommandInfo extends ApplicationCommandInfo {
	private final Object instance;
	private final MethodParameters<ContextCommandParameter<MessageContextParameterResolver>> commandParameters;

	public MessageCommandInfo(ApplicationCommand instance, Method method) {
		super(instance, method.getAnnotation(JdaMessageCommand.class),
				method,
				method.getAnnotation(JdaMessageCommand.class).name());

		this.instance = instance;

		final Class<?>[] parameterTypes = method.getParameterTypes();
		
		if (!GlobalMessageEvent.class.isAssignableFrom(parameterTypes[0])) {
			throw new IllegalArgumentException("First argument should be a GlobalUserEvent for method " + Utils.formatMethodShort(method));
		}

		this.commandParameters = MethodParameters.of(method, (parameter, i) -> {
			return new ContextCommandParameter<>(MessageContextParameterResolver.class, parameter, i);
		});
	}

	public boolean execute(BContext context, MessageContextCommandEvent event) {
		try {
			final Object[] objects = new Object[commandParameters.size() + 1];
			if (guildOnly) {
				objects[0] = new GuildMessageEvent(context, event);
			} else {
				objects[0] = new GlobalMessageEvent(context, event);
			}
			
			for (int i = 0, commandParametersLength = commandParameters.size(); i < commandParametersLength; i++) {
				ContextCommandParameter<MessageContextParameterResolver> parameter = commandParameters.get(i);

				if (parameter.isOption()) {
					objects[i + 1] = parameter.getResolver().resolve(event);
				} else {
					objects[i + 1] = parameter.getCustomResolver().resolve(event);
				}
			}

			commandMethod.invoke(instance, objects);

			return true;
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MethodParameters<? extends ApplicationCommandParameter<?>> getParameters() {
		return commandParameters;
	}
}
