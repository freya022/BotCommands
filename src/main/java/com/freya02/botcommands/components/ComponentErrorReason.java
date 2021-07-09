package com.freya02.botcommands.components;

public enum ComponentErrorReason {
	NOT_OWNER(null),
	EXPIRED("This component is not associated with an action anymore"),
	DONT_EXIST("This component could not be found");

	private String reason;

	ComponentErrorReason(String reason) {
		setReason(reason);
	}

	public ComponentErrorReason setReason(String reason) {
		this.reason = reason;

		return this;
	}

	public String getReason() {
		return reason;
	}
}
