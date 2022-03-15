package com.freya02.botcommands.internal.modals;

import com.freya02.botcommands.api.modals.ModalTimeoutInfo;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class ModalData {
	private final String handlerName;
	private final Object[] userData;
	private final Map<String, InputData> inputDataMap;
	private final ModalTimeoutInfo timeoutInfo;

	private ScheduledFuture<?> timeoutFuture;

	public ModalData(String handlerName, Object[] userData, Map<String, InputData> inputDataMap, ModalTimeoutInfo timeoutInfo) {
		this.handlerName = handlerName;
		this.userData = userData;
		this.inputDataMap = inputDataMap;
		this.timeoutInfo = timeoutInfo;
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

	public ModalTimeoutInfo getTimeoutInfo() {
		return timeoutInfo;
	}

	public void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) {
		this.timeoutFuture = timeoutFuture;
	}

	public void cancelTimeoutFuture() {
		if (timeoutFuture != null) {
			timeoutFuture.cancel(false);
		}
	}
}
