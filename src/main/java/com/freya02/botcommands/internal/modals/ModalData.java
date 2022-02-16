package com.freya02.botcommands.internal.modals;

public class ModalData {
	private final String handlerName;
	private final Object[] userData;

	public ModalData(String handlerName, Object[] userData) {
		this.handlerName = handlerName;
		this.userData = userData;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public Object[] getUserData() {
		return userData;
	}
}
