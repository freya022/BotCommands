package com.freya02.botcommands.internal.events;

import com.freya02.botcommands.internal.application.CommandParameter;

import java.lang.reflect.Parameter;

public class EventListenerParameter extends CommandParameter<Object> {
	public EventListenerParameter(Parameter parameter, int index) {
		super(null, parameter, index);
	}
}
