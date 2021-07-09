package com.freya02.botcommands.components.internal.xodus;

public class XodusLambdaComponentData extends XodusComponentData {
	private final long handlerId;

	public XodusLambdaComponentData(String componentId, boolean oneUse, long expirationTimestamp, long ownerId, long handlerId) {
		super(componentId, -1, oneUse, expirationTimestamp, ownerId);

		this.handlerId = handlerId;
	}

	public long getHandlerId() {
		return handlerId;
	}

	@Override
	public String toString() {
		return "XodusLambdaComponentData{" +
				"handlerId=" + handlerId +
				"} " + super.toString();
	}
}
