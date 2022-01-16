package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.DefaultMessages;

import java.util.function.Function;

public enum ComponentErrorReason {
	NOT_ALLOWED(DefaultMessages::getComponentNotAllowedErrorMsg),
	EXPIRED(DefaultMessages::getComponentExpiredErrorMsg),
	NOT_FOUND(DefaultMessages::getComponentNotFoundErrorMsg),
	INVALID_DATA(DefaultMessages::getComponentInvalidDataErrorMsg);

	private final Function<DefaultMessages, String> reasonProvider;

	ComponentErrorReason(Function<DefaultMessages, String> reasonProvider) {
		this.reasonProvider = reasonProvider;
	}

	public String getReason(DefaultMessages defaultMessages) {
		return reasonProvider.apply(defaultMessages);
	}
}
