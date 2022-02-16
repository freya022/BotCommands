package com.freya02.botcommands.internal.modals;

import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.concurrent.ThreadLocalRandom;

public class ModalMaps {
	private static final long MAX_ID = Long.MAX_VALUE;
	private static final long MIN_ID = (long) Math.pow(10, Math.floor(Math.log10(MAX_ID))); //Same amount of digits except every digit is 0 but the first one is 1

	private final TLongObjectHashMap<?> modalMap = new TLongObjectHashMap<>();
	private final TLongObjectHashMap<?> inputMap = new TLongObjectHashMap<>();

	public String newModalId() {
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		long id;

		synchronized (modalMap) {
			do {
				id = random.nextLong(MIN_ID, MAX_ID);
			} while (modalMap.containsKey(id));
		}

		return String.valueOf(id);
	}

	public String newInputId() {
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		long id;

		synchronized (inputMap) {
			do {
				id = random.nextLong(MIN_ID, MAX_ID);
			} while (inputMap.containsKey(id));
		}

		return String.valueOf(id);
	}
}
