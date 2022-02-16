package com.freya02.botcommands.internal.modals;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.ExecutableInteractionInfo;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.runner.MethodRunner;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ModalHandlerInfo implements ExecutableInteractionInfo {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	private final Object autocompletionHandler;
	private final Method method;
	private final MethodRunner methodRunner;

	private final String handlerName;
	private final MethodParameters<ModalHandlerParameter> modalParameters;

	public ModalHandlerInfo(BContextImpl context, Object autocompletionHandler, Method method) {
		this.context = context;
		this.autocompletionHandler = autocompletionHandler;
		this.method = method;
		this.methodRunner = context.getMethodRunnerFactory().make(autocompletionHandler, method);

		final ModalHandler annotation = method.getAnnotation(ModalHandler.class);
		this.handlerName = annotation.name();

		this.modalParameters = MethodParameters.of(context, method, ModalHandlerParameter::new);
	}

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
	public MethodParameters<ModalHandlerParameter> getParameters() {
		return modalParameters;
	}

	@Override
	@NotNull
	public Object getInstance() {
		return autocompletionHandler;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public boolean execute(BContext context, ModalData modalData, ModalInteractionEvent event, Consumer<Throwable> throwableConsumer) throws Exception {
		final Map<String, InputData> inputDataMap = modalData.getInputDataMap();
		final Map<String, String> inputNameToInputIdMap = new HashMap<>();

		inputDataMap.forEach((inputId, inputData) -> {
			inputNameToInputIdMap.put(inputData.getInputName(), inputId);
		});

		List<Object> objects = new ArrayList<>(modalParameters.size() + 1);

		objects.add(event);

		final Object[] userData = modalData.getUserData();

		//Check if there's enough arguments to fit user data
		if (modalParameters.size() < 1 + userData.length) {
			throw new IllegalArgumentException("Method at %s only has %d @%s parameters, but you gave %d parameters in the modal declaration".formatted(Utils.formatMethodShort(method),
					modalParameters.stream().filter(ModalHandlerParameter::isModalData).count(),
					com.freya02.botcommands.api.modals.annotations.ModalData.class.getSimpleName(),
					userData.length
			));
		}

		for (int i = 0; i < userData.length; i++) {
			final ModalHandlerParameter parameter = modalParameters.get(i);

			if (!parameter.isModalData()) throw new IllegalArgumentException();

			final Object data = userData[i];

			if (!parameter.getBoxedType().isAssignableFrom(data.getClass())) {
				//TODO localize
				// could be really nice to have a class which holds a version of the user-localized error and the dev-localized errors,
				// based off the same parameters, using the new localization API
				event.replyFormat("The parameter '%s' is not a valid type (expected a %s, got a %s)", parameter.getParameter().getName(), parameter.getBoxedType().getSimpleName(), data.getClass().getSimpleName())
						.setEphemeral(true)
						.queue();

				LOGGER.error("The modal user data '{}' is not a valid type (expected a {}, got a {})", parameter.getParameter().getName(), parameter.getBoxedType().getSimpleName(), data.getClass().getSimpleName());

				return false;
			}

			objects.add(data);
		}

		for (ModalHandlerParameter parameter : modalParameters) {
			if (parameter.isModalData()) continue; //We already processed modal data

			final Object obj;

			if (parameter.isOption() && parameter.isModalInput()) {
				//We have the modal input's ID
				// But we have a Map of input *name* -> InputData (contains input ID)

				final String inputId = inputNameToInputIdMap.get(parameter.getModalInputName());
				final ModalMapping modalMapping = event.getValue(inputId);

				if (modalMapping == null) {
					throw new IllegalArgumentException(String.format("Modal input '%s' was not found", parameter.getModalInputName()));
				}

				obj = parameter.getResolver().resolve(context, this, event, modalMapping);

				if (obj == null) {
					//TODO localize, see above
					event.replyFormat(context.getDefaultMessages(event.getGuild()).getSlashCommandUnresolvableParameterMsg(), parameter.getParameter().getName(), parameter.getBoxedType().getSimpleName())
							.setEphemeral(true)
							.queue();

					//Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
					LOGGER.trace("The parameter '{}' of value '{}' could not be resolved into a {}", parameter.getParameter().getName(), modalMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return false;
				}

				if (!parameter.getBoxedType().isAssignableFrom(obj.getClass())) {
					//TODO localize, see above
					event.replyFormat(context.getDefaultMessages(event.getGuild()).getSlashCommandInvalidParameterTypeMsg(), parameter.getParameter().getName(), modalMapping.getAsString(), parameter.getBoxedType().getSimpleName())
							.setEphemeral(true)
							.queue();

					LOGGER.error("The parameter '{}' of value '{}' is not a valid type (expected a {})", parameter.getParameter().getName(), modalMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return false;
				}
			} else {
				obj = parameter.getCustomResolver().resolve(context, this, event);
			}

			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			objects.add(obj);
		}

		getMethodRunner().invoke(objects.toArray(), throwableConsumer);

		return true;
	}
}
