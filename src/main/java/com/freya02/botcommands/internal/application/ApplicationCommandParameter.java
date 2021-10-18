package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.internal.ApplicationOptionData;

import java.lang.reflect.Parameter;

public abstract class ApplicationCommandParameter<RESOLVER> extends CommandParameter<RESOLVER> {
	private final ApplicationOptionData applicationOptionData;

	public ApplicationCommandParameter(Class<RESOLVER> resolverType, Parameter parameter, int index) {
		super(resolverType, parameter, index);

		if (parameter.isAnnotationPresent(AppOption.class)) {
			this.applicationOptionData = new ApplicationOptionData(parameter);
		} else {
			this.applicationOptionData = null;
		}
	}

	public ApplicationOptionData getApplicationOptionData() {
		return applicationOptionData;
	}
}
