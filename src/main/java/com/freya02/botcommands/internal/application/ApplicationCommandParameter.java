package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.utils.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

public abstract class ApplicationCommandParameter<RESOLVER> extends CommandParameter<RESOLVER> {
	private final ApplicationOptionData applicationOptionData;

	public ApplicationCommandParameter(Class<RESOLVER> resolverType, Parameter parameter, int index) {
		this(resolverType, parameter, Utils.getBoxedType(parameter.getType()), index);
	}

	public ApplicationCommandParameter(Class<RESOLVER> resolverType, Parameter parameter, Class<?> boxedType, int index) {
		super(resolverType, parameter, boxedType, index);

		if (parameter.isAnnotationPresent(AppOption.class)) {
			this.applicationOptionData = new ApplicationOptionData(parameter);
		} else {
			this.applicationOptionData = null;
		}
	}

	@Override
	protected List<Class<? extends Annotation>> getOptionAnnotations() {
		return List.of(AppOption.class);
	}

	public ApplicationOptionData getApplicationOptionData() {
		return applicationOptionData;
	}
}
