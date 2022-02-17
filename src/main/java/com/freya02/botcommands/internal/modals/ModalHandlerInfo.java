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

	private final Object autocompletionHandler;
	private final Method method;
	private final MethodRunner methodRunner;

	private final String handlerName;
	private final MethodParameters<ModalHandlerParameter> modalParameters;

	public ModalHandlerInfo(BContextImpl context, Object autocompletionHandler, Method method) {
		this.autocompletionHandler = autocompletionHandler;
		this.method = method;
		this.methodRunner = context.getMethodRunnerFactory().make(autocompletionHandler, method);

		final ModalHandler annotation = method.getAnnotation(ModalHandler.class);
		this.handlerName = annotation.name();

		this.modalParameters = MethodParameters.of(context, method, ModalHandlerParameter::new);

		final boolean hasModalData = modalParameters.stream().anyMatch(ModalHandlerParameter::isModalData);

		//Check if the first parameters are all modal data
		if (hasModalData) {
			boolean sawModalData = false;
			for (ModalHandlerParameter parameter : modalParameters) {
				if (!parameter.isModalData() && !sawModalData)
					throw new IllegalArgumentException(("Parameter #%d at %s must be annotated with @%s or situated after all modal data parameters.\n" +
							"All modal data must be inserted after the event, with the same order as the constructed modal, before inserting modal inputs and custom parameters").formatted(parameter.getIndex(),
							Utils.formatMethodShort(method),
							ModalData.class.getSimpleName()));

				if (parameter.isModalData())
					sawModalData = true;
			}
		}
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

		final long expectedModalDatas = modalParameters.stream().filter(ModalHandlerParameter::isModalData).count();
		final long expectedModalInputs = modalParameters.stream().filter(ModalHandlerParameter::isModalInput).count();

		//Check if there's enough arguments to fit user data + modal inputs
		if (expectedModalDatas != userData.length
				|| expectedModalInputs != event.getValues().size()) {
			throw new IllegalArgumentException("""
					Modal handler at %s does not match the received modal data:
					Method signature: %d userdata parameters and %d modal input(s)
					Discord data: %d userdata parameters and %d modal input(s)""".formatted(
					Utils.formatMethodShort(method),
					expectedModalDatas,
					expectedModalInputs,
					userData.length,
					event.getValues().size()
			));
		}

		//Insert modal data in the order of appearance, after the event
		for (int i = 0; i < userData.length; i++) {
			final ModalHandlerParameter parameter = modalParameters.get(i);

			if (!parameter.isModalData()) //Should be caught by the constructor
				throw new IllegalArgumentException(("Parameter #%d at %s must be annotated with @%s or situated after all modal data parameters.\n" +
						"All modal data must be inserted after the event, with the same order as the constructed modal, before inserting modal inputs and custom parameters").formatted(i,
						Utils.formatMethodShort(method),
						ModalData.class.getSimpleName()));

			final Object data = userData[i];

			if (!parameter.getBoxedType().isAssignableFrom(data.getClass())) {
				//TODO localize
				// could be really nice to have a class which holds a version of the user-localized error and the dev-localized errors,
				// based off the same parameters, using the new localization API

				//TODO Should these be removed in favor of just throwing an exception ? The user doesn't need to know errors that specific,
				// the dev already receives the error by DMs (and by looking at logs)
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

				if (inputId == null) {
					throw new IllegalArgumentException(String.format("Modal input '%s' was not found", parameter.getModalInputName()));
				}

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
