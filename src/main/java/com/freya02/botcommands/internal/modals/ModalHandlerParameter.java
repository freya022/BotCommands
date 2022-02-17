package com.freya02.botcommands.internal.modals;

import com.freya02.botcommands.api.modals.annotations.ModalData;
import com.freya02.botcommands.api.modals.annotations.ModalInput;
import com.freya02.botcommands.api.parameters.ModalParameterResolver;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.utils.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class ModalHandlerParameter extends CommandParameter<ModalParameterResolver> {
	private final boolean isModalData;
	private final boolean isModalInput;
	private final String modalInputName;

	public ModalHandlerParameter(Parameter parameter, int index) {
		super(ModalParameterResolver.class, parameter, index);

		this.isModalData = parameter.isAnnotationPresent(ModalData.class);
		this.isModalInput = parameter.isAnnotationPresent(ModalInput.class);

		if (isModalData && isModalInput)  {
			throw new IllegalArgumentException("Parameter #%d of %s cannot be both modal data and modal input".formatted(index, Utils.formatMethodShort((Method) parameter.getDeclaringExecutable())));
		}

		if (isModalInput) {
			this.modalInputName = parameter.getAnnotation(ModalInput.class).name();
		} else {
			this.modalInputName = null;
		}
	}

	public boolean isModalData() {
		return isModalData;
	}

	public boolean isModalInput() {
		return isModalInput;
	}

	public String getModalInputName() {
		return modalInputName;
	}

	@Override
	protected List<Class<? extends Annotation>> getOptionAnnotations() {
		return List.of(ModalData.class, ModalInput.class);
	}

	@Override
	protected List<Class<? extends Annotation>> getResolvableAnnotations() {
		return List.of(ModalInput.class);
	}
}
