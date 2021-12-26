package com.freya02.botcommands.internal.application.context.user;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent;
import com.freya02.botcommands.api.application.context.user.GuildUserEvent;
import com.freya02.botcommands.api.parameters.UserContextParameterResolver;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;
import com.freya02.botcommands.internal.application.context.ContextCommandParameter;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.UserContextEvent;

import java.lang.reflect.Method;

public class UserCommandInfo extends ApplicationCommandInfo {
	private final Object instance;
	private final MethodParameters<ContextCommandParameter<UserContextParameterResolver>> commandParameters;

	public UserCommandInfo(ApplicationCommand instance, Method method) {
		super(instance, method.getAnnotation(JDAUserCommand.class),
				method,
				method.getAnnotation(JDAUserCommand.class).name());

		this.instance = instance;

		final Class<?>[] parameterTypes = method.getParameterTypes();
		
		if (!GlobalUserEvent.class.isAssignableFrom(parameterTypes[0])) {
			throw new IllegalArgumentException("First argument should be a GlobalUserEvent for method " + Utils.formatMethodShort(method));
		}

		this.commandParameters = MethodParameters.of(method, (parameter, i) -> {
			if (parameter.isAnnotationPresent(TextOption.class))
				throw new IllegalArgumentException(String.format("User command parameter #%d of %s#%s cannot be annotated with @TextOption", i, commandMethod.getDeclaringClass().getName(), commandMethod.getName()));

			final Class<?> type = parameter.getType();

			if (Member.class.isAssignableFrom(type)) {
				if (!isGuildOnly())
					throw new IllegalArgumentException("The user command " + Utils.formatMethodShort(commandMethod) + " cannot have a " + type.getSimpleName() + " parameter as it is not guild-only");
			}

			return new ContextCommandParameter<>(UserContextParameterResolver.class, parameter, i);
		});
	}

	public boolean execute(BContext context, UserContextEvent event) throws Exception {
		final Object[] objects = new Object[commandParameters.size() + 1];
		if (guildOnly) {
			objects[0] = new GuildUserEvent(context, event);
		} else {
			objects[0] = new GlobalUserEvent(context, event);
		}

		for (int i = 0, commandParametersLength = commandParameters.size(); i < commandParametersLength; i++) {
			ContextCommandParameter<UserContextParameterResolver> parameter = commandParameters.get(i);

			if (parameter.isOption()) {
				objects[i + 1] = parameter.getResolver().resolve(event);
				//no need to check for unresolved parameters,
				// it is impossible to have other arg types other than User (and custom resolvers)
			} else {
				objects[i + 1] = parameter.getCustomResolver().resolve(event);
			}
		}

		applyCooldown(event);

		commandMethod.invoke(instance, objects);

		return true;
	}

	@Override
	public MethodParameters<? extends ApplicationCommandParameter<?>> getParameters() {
		return commandParameters;
	}
}
