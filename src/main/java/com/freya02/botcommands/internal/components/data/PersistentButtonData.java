package com.freya02.botcommands.internal.components.data;

public class PersistentButtonData {
	private final String handlerName;
	private final String[] args;

	public PersistentButtonData(String handlerName, String[] args) {
		this.handlerName = handlerName;
		this.args = args;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public String[] getArgs() {
		return args;
	}
}
