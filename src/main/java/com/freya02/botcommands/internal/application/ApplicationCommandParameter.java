package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.annotations.api.application.annotations.AppOption;
import com.freya02.botcommands.internal.ApplicationOptionData;
import kotlin.reflect.KParameter;
import kotlin.reflect.KType;

import java.lang.annotation.Annotation;
import java.util.List;

public abstract class ApplicationCommandParameter<RESOLVER> extends CommandParameter<RESOLVER> {
	private final ApplicationOptionData applicationOptionData;

	public ApplicationCommandParameter(Class<RESOLVER> resolverType, KParameter parameter, int index) {
		this(resolverType, parameter, parameter.getType(), index);
	}

	public ApplicationCommandParameter(Class<RESOLVER> resolverType, KParameter parameter, KType boxedType, int index) {
		super(resolverType, parameter, boxedType, index);

		this.applicationOptionData = null; //TODO fix
//		if (parameter.isAnnotationPresent(AppOption.class)) {
//			this.applicationOptionData = new ApplicationOptionData(parameter);
//		} else {
//			this.applicationOptionData = null;
//		}
	}

	@Override
	protected List<Class<? extends Annotation>> getOptionAnnotations() {
		return List.of(AppOption.class);
	}

	public ApplicationOptionData getApplicationOptionData() {
		return applicationOptionData;
	}
}
