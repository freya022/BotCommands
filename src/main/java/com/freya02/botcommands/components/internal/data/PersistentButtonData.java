package com.freya02.botcommands.components.internal.data;

import java.util.List;

public class PersistentButtonData {
	private final String handlerName;
	private final List<String> args;

	public PersistentButtonData(String handlerName, List<String> args) {
		this.handlerName = handlerName;
		this.args = args;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public List<String> getArgs() {
		return args;
	}
}
