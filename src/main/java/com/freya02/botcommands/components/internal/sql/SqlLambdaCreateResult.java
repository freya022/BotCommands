package com.freya02.botcommands.components.internal.sql;

public class SqlLambdaCreateResult {
	private final String componentId;
	private final long handlerId;

	public SqlLambdaCreateResult(String componentId, long handlerId) {
		this.componentId = componentId;
		this.handlerId = handlerId;
	}

	public String getComponentId() {
		return componentId;
	}

	public long getHandlerId() {
		return handlerId;
	}
}
