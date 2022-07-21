package com.freya02.botcommands.internal.application.context.user;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent;
import com.freya02.botcommands.api.application.context.user.GuildUserEvent;
import com.freya02.botcommands.api.parameters.UserContextParameterResolver;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.application.context.ContextCommandParameter;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class UserCommandInfo extends ApplicationCommandInfo {
	private final MethodParameters<ContextCommandParameter<UserContextParameterResolver>> commandParameters;

	public UserCommandInfo(BContext context, ApplicationCommand instance, Method method) {
		super(context, instance,
				method.getAnnotation(JDAUserCommand.class),
				method,
				JDAUserCommand::name);

		final Class<?>[] parameterTypes = method.getParameterTypes();
		
		if (!GlobalUserEvent.class.isAssignableFrom(parameterTypes[0])) {
			throw new IllegalArgumentException("First argument should be a GlobalUserEvent for method " + Utils.formatMethodShort(method));
		}

		this.commandParameters = MethodParameters.of(context, method, (parameter, i) -> {
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

	public boolean execute(BContextImpl context, UserContextInteractionEvent event, Consumer<Throwable> throwableConsumer) throws Exception {
		final Object[] objects = new Object[commandParameters.size() + 1];
		if (guildOnly) {
			objects[0] = new GuildUserEvent(getMethod(), context, event);
		} else {
			objects[0] = new GlobalUserEvent(getMethod(), context, event);
		}

		for (int i = 0, commandParametersLength = commandParameters.size(); i < commandParametersLength; i++) {
			ContextCommandParameter<UserContextParameterResolver> parameter = commandParameters.get(i);

			if (parameter.isOption()) {
				objects[i + 1] = parameter.getResolver().resolve(context, this, event);
				//no need to check for unresolved parameters,
				// it is impossible to have other arg types other than User (and custom resolvers)
			} else {
				objects[i + 1] = parameter.getCustomResolver().resolve(context, this, event);
			}
		}

		applyCooldown(event);

		getMethodRunner().invoke(objects, throwableConsumer);

		return true;
	}

	@Override
	@NotNull
	public MethodParameters<ContextCommandParameter<UserContextParameterResolver>> getParameters() {
		return commandParameters;
	}
}
