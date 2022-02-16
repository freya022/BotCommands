package com.freya02.botcommands.internal.application.context.message;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;
import com.freya02.botcommands.api.parameters.MessageContextParameterResolver;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.application.context.ContextCommandParameter;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class MessageCommandInfo extends ApplicationCommandInfo {
	private final MethodParameters<ContextCommandParameter<MessageContextParameterResolver>> commandParameters;

	public MessageCommandInfo(BContext context, ApplicationCommand instance, Method method) {
		super(context, instance, method.getAnnotation(JDAMessageCommand.class),
				method,
				method.getAnnotation(JDAMessageCommand.class).name());

		final Class<?>[] parameterTypes = method.getParameterTypes();
		
		if (!GlobalMessageEvent.class.isAssignableFrom(parameterTypes[0])) {
			throw new IllegalArgumentException("First argument should be a GlobalUserEvent for method " + Utils.formatMethodShort(method));
		}

		this.commandParameters = MethodParameters.of(context, method, (parameter, i) -> {
			if (parameter.isAnnotationPresent(TextOption.class))
				throw new IllegalArgumentException(String.format("Message command parameter #%d of %s#%s cannot be annotated with @TextOption", i, commandMethod.getDeclaringClass().getName(), commandMethod.getName()));

			return new ContextCommandParameter<>(MessageContextParameterResolver.class, parameter, i);
		});
	}

	public boolean execute(BContext context, MessageContextInteractionEvent event, Consumer<Throwable> throwableConsumer) throws Exception {
		final Object[] objects = new Object[commandParameters.size() + 1];
		if (guildOnly) {
			objects[0] = new GuildMessageEvent(context, event);
		} else {
			objects[0] = new GlobalMessageEvent(context, event);
		}

		for (int i = 0, commandParametersLength = commandParameters.size(); i < commandParametersLength; i++) {
			ContextCommandParameter<MessageContextParameterResolver> parameter = commandParameters.get(i);

			if (parameter.isOption()) {
				objects[i + 1] = parameter.getResolver().resolve(context, this, event);

				//no need to check for unresolved parameters,
				// it is impossible to have other arg types other than Message (and custom resolvers)
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
	public MethodParameters<ContextCommandParameter<MessageContextParameterResolver>> getParameters() {
		return commandParameters;
	}
}
