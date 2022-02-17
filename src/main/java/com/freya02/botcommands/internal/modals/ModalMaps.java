package com.freya02.botcommands.internal.modals;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

public class ModalMaps {
	private static final ScheduledExecutorService TIMEOUT_SERVICE = Executors.newSingleThreadScheduledExecutor();

	private static final long MAX_ID = Long.MAX_VALUE;
	private static final long MIN_ID = (long) Math.pow(10, Math.floor(Math.log10(MAX_ID))); //Same amount of digits except every digit is 0 but the first one is 1

	private final Map<String, ModalData> modalMap = new HashMap<>();

	//Modals input IDs are temporarily stored here while it waits for its ModalBuilder owner to be built, and it's InputData to be associated with it
	private final Map<String, InputData> inputMap = new HashMap<>();

	private String nextModalId() {
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		synchronized (modalMap) {
			String id;

			do {
				id = String.valueOf(random.nextLong(MIN_ID, MAX_ID));
			} while (modalMap.containsKey(id));

			return id;
		}
	}

	public String insertModal(ModalData data, String id) {
		synchronized (modalMap) {
			if (id == null || id.equals("0")) {
				id = nextModalId();
			}

			modalMap.put(id, data);

			if (data.getTimeoutInfo() != null) {
				final String finalId = id;

				final ScheduledFuture<?> future = TIMEOUT_SERVICE.schedule(() -> {
					synchronized (modalMap) {
						if (modalMap.remove(finalId) != null) { //If the timeout was reached without the modal being used
							data.getTimeoutInfo().onTimeout().run();
						}
					}
				}, data.getTimeoutInfo().timeout(), data.getTimeoutInfo().unit());

				data.setTimeoutFuture(future);
			}
		}

		return id;
	}

	@Nullable
	public ModalData consumeModal(String modalId) {
		synchronized (modalMap) {
			final ModalData data = modalMap.remove(modalId);
			data.cancelTimeoutFuture();

			return data;
		}
	}

	private String nextInputId() {
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		synchronized (inputMap) {
			String id;

			do {
				id = String.valueOf(random.nextLong(MIN_ID, MAX_ID));
			} while (inputMap.containsKey(id));

			return id;
		}
	}

	public String insertInput(InputData data, String id) {
		synchronized (inputMap) {
			if (id == null || id.equals("0")) {
				id = nextInputId();
			}

			inputMap.put(id, data);
		}

		return id;
	}

	@Nullable
	public InputData removeInput(String inputId) {
		synchronized (inputMap) {
			return inputMap.remove(inputId);
		}
	}
}
