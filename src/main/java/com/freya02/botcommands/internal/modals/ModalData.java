package com.freya02.botcommands.internal.modals;

import java.util.Map;

public class ModalData {
	private final String handlerName;
	private final Object[] userData;
	private final Map<String, InputData> inputDataMap;

	public ModalData(String handlerName, Object[] userData, Map<String, InputData> inputDataMap) {
		this.handlerName = handlerName;
		this.userData = userData;
		this.inputDataMap = inputDataMap;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public Object[] getUserData() {
		return userData;
	}

	public Map<String, InputData> getInputDataMap() {
		return inputDataMap;
	}
}
