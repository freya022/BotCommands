package com.freya02.botcommands.internal.events;

import com.freya02.botcommands.internal.application.CommandParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

public class EventListenerParameter extends CommandParameter<Object> {
	public EventListenerParameter(Parameter parameter, int index) {
		super(null, parameter, index);
	}

	@Override
	protected List<Class<? extends Annotation>> getOptionAnnotations() {
		return List.of();
	}
}
