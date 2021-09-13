package com.freya02.botcommands.internal.components.data;

public class PersistentSelectionMenuData {
	private final String handlerName;
	private final String[] args;

	public PersistentSelectionMenuData(String handlerName, String[] args) {
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
