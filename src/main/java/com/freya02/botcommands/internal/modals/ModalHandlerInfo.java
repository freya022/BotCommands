package com.freya02.botcommands.internal.modals;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.ExecutableInteractionInfo;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompleteCommandParameter;
import com.freya02.botcommands.internal.runner.MethodRunner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class ModalHandlerInfo implements ExecutableInteractionInfo {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	private final Object autocompletionHandler;
	private final Method method;
	private final MethodRunner methodRunner;

	private final String handlerName;
	private final MethodParameters<AutocompleteCommandParameter> modalParameters;

	public ModalHandlerInfo(BContextImpl context, Object autocompletionHandler, Method method) {
		this.context = context;
		this.autocompletionHandler = autocompletionHandler;
		this.method = method;
		this.methodRunner = context.getMethodRunnerFactory().make(autocompletionHandler, method);

		final ModalHandler annotation = method.getAnnotation(ModalHandler.class);
		this.handlerName = annotation.name();

		this.modalParameters = MethodParameters.of(context, method, AutocompleteCommandParameter::new);
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
	public MethodParameters<AutocompleteCommandParameter> getParameters() {
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
}
