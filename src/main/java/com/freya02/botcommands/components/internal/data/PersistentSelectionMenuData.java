package com.freya02.botcommands.components.internal.data;

import java.util.List;

public class PersistentSelectionMenuData {
	private final String handlerName;
	private final List<String> args;

	public PersistentSelectionMenuData(String handlerName, List<String> args) {
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
