package com.freya02.botcommands.api.components;

public enum ComponentErrorReason {
	NOT_ALLOWED(null),
	EXPIRED("This component is not usable anymore (expired)"),
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
