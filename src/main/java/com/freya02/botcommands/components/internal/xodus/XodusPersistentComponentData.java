package com.freya02.botcommands.components.internal.xodus;

import java.util.List;

public class XodusPersistentComponentData extends XodusComponentData {
	private final String handlerName;
	private final List<String> args;

	public XodusPersistentComponentData(String componentId, boolean oneUse, long expirationTimestamp, long ownerId, String handlerName, List<String> args) {
		super(componentId, -1, oneUse, expirationTimestamp, ownerId);

		this.handlerName = handlerName;
		this.args = args;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public List<String> getArgs() {
		return args;
	}

	@Override
	public String toString() {
		return "XodusPersistentComponentData{" +
				"handlerName='" + handlerName + '\'' +
				", args=" + args +
				"} " + super.toString();
	}
}
