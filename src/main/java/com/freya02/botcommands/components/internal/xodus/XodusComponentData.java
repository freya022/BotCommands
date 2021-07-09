package com.freya02.botcommands.components.internal.xodus;

@SuppressWarnings("unused")
public class XodusComponentData {
	private final String componentId;
	private final long groupId;

	private final boolean oneUse;
	private final long expirationTimestamp;
	private final long ownerId;

	public XodusComponentData(String componentId, long groupId, boolean oneUse, long expirationTimestamp, long ownerId) {
		this.componentId = componentId;
		this.groupId = groupId;
		this.oneUse = oneUse;
		this.expirationTimestamp = expirationTimestamp;
		this.ownerId = ownerId;
	}

	public String getComponentId() {
		return componentId;
	}

	public long getGroupId() {
		return groupId;
	}

	public boolean isOneUse() {
		return oneUse;
	}

	public long getExpirationTimestamp() {
		return expirationTimestamp;
	}

	public long getOwnerId() {
		return ownerId;
	}

	@Override
	public String toString() {
		return "XodusComponentData{" +
				"componentId='" + componentId + '\'' +
				", groupId=" + groupId +
				", oneUse=" + oneUse +
				", expirationTimestamp=" + expirationTimestamp +
				", ownerId=" + ownerId +
				'}';
	}
}
