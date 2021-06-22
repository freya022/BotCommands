package com.freya02.botcommands.buttons;

import com.freya02.botcommands.Logging;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.core.dataStructures.hash.IntHashMap;
import jetbrains.exodus.env.*;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Provides a default implementation for a persistent component ID manager
 * <br>
 * <i>Uses Xodus as a server-less database</i>
 */
public class DefaultIdManager implements IdManager {
	@SuppressWarnings("SpellCheckingInspection")
	private static final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&'()*+,-./:;<=>?_^[]{}|".toCharArray();
	private static final Logger LOGGER = Logging.getLogger();

	private final Environment env;
	private final Store idStore, tempIdStore;

	private final IntHashMap<ButtonConsumer> actionMap = new IntHashMap<>();
	private int handlerId = 0;

	/**
	 * Creates a default ID manager for Discord components
	 *
	 * @param dbPath Path of the database folder, must exist
	 */
	public DefaultIdManager(Path dbPath) {
		if (Files.notExists(dbPath))
			throw new IllegalStateException("Path " + dbPath.toAbsolutePath() + " does not exist");

		if (!Files.isDirectory(dbPath))
			throw new IllegalStateException("Path " + dbPath.toAbsolutePath() + " is not a directory");

		env = Environments.newInstance(dbPath.toFile(), new EnvironmentConfig().setEnvCloseForcedly(true));
		Runtime.getRuntime().addShutdownHook(new Thread(env::close));

		idStore = env.computeInTransaction(txn -> env.openStore("ComponentIDs", StoreConfig.WITHOUT_DUPLICATES, txn));
		tempIdStore = env.computeInTransaction(txn -> {
			if (env.storeExists("TempComponentIDs", txn)) {
				env.truncateStore("TempComponentIDs", txn);
			}

			return env.openStore("TempComponentIDs", StoreConfig.WITHOUT_DUPLICATES, txn);
		});
	}

	private static String random(int n) {
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		final StringBuilder sb = new StringBuilder(64);
		for (int i = 0; i < n; i++) {
			sb.append(chars[random.nextInt(0, chars.length)]);
		}

		return sb.toString();
	}

	@Override
	public String getContent(String buttonId) {
		return env.computeInReadonlyTransaction(txn -> {
			final ArrayByteIterable idEntry = StringBinding.stringToEntry(buttonId);

			final ByteIterable entry;
			final boolean isTemporary = buttonId.length() == 64;
			if (isTemporary) { //Temporary ID
				entry = tempIdStore.get(txn, idEntry);
			} else { //Normal handler ID
				entry = idStore.get(txn, idEntry);
			}

			if (entry == null) {
				if (isTemporary) {
					LOGGER.debug("Temporary button ID {} not found in database", buttonId);
				} else {
					LOGGER.info("Button ID {} not found in database", buttonId);
				}
				return null;
			} else {
				return StringBinding.entryToString(entry);
			}
		});
	}

	@Override
	public String newId(String content, boolean temporary) {
		return env.computeInTransaction(txn -> {
			final Store store = temporary ? tempIdStore : idStore;
			ArrayByteIterable entry;
			String buttonId;

			do {
				buttonId = random(temporary ? 64 : 96);
			} while (store.get(txn, (entry = StringBinding.stringToEntry(buttonId))) != null);

			store.put(txn, entry, StringBinding.stringToEntry(content));

			return buttonId;
		});
	}

	@Override
	public void removeId(String buttonId, boolean isTemporary) {
		env.executeInTransaction(txn -> {
			try (Cursor cursor = isTemporary ? tempIdStore.openCursor(txn) : idStore.openCursor(txn)) {
				final ByteIterable value = cursor.getSearchKey(StringBinding.stringToEntry(buttonId));

				if (value == null) {
					LOGGER.warn("Tried to delete key '{}' but it was not found in the {} Store", buttonId, isTemporary ? "temporary" : "persistent");
					return;
				}

				deleteId(isTemporary, cursor);
			}
		});
	}

	@Override
	public void removeIds(Collection<String> buttonIds) {
		env.executeInTransaction(txn -> {
			boolean temp = true;

			for (String buttonId : buttonIds) {
				final ArrayByteIterable key = StringBinding.stringToEntry(buttonId);

				Cursor cursor = tempIdStore.openCursor(txn);
				ByteIterable value = cursor.getSearchKey(key);
				if (value == null) {
					temp = false;
					cursor.close();
					cursor = idStore.openCursor(txn);

					value = cursor.getSearchKey(key);
				}

				if (value == null) {
					LOGGER.warn("Tried to delete key '{}' but it was not found in both Stores", buttonId);
					return;
				}

				deleteId(temp, cursor);

				cursor.close();
			}
		});
	}

	private void deleteId(boolean isTemporary, Cursor cursor) {
		//Removing a temporary button's ID is not really the priority
		// Most important is removing the Consumer reference, so you can also free up memory claimed by the lambda's fields
		if (isTemporary) { //Temporary button
			final ByteIterable value = cursor.getValue();
			final String content = StringBinding.entryToString(value);

			actionMap.remove(Integer.parseInt(content.substring(content.lastIndexOf('|') + 1)));
		}

		if (!cursor.deleteCurrent()) {
			LOGGER.warn("Unable to delete current cursor value, key: '{}', value: '{}', please report to the devs", StringBinding.entryToString(cursor.getKey()), StringBinding.entryToString(cursor.getValue()));
		}
	}

	@Override
	public ButtonConsumer getAction(int handlerId) {
		return actionMap.get(handlerId);
	}

	@Override
	public int newHandlerId(ButtonConsumer action) {
		final int handlerId = getNextHandlerId();

		if (actionMap.put(handlerId, action) != null) {
			LOGGER.warn("Overwrote handler {}, please report to the devs", handlerId);
		}

		return handlerId;
	}

	/**
	 * I doubt you'll hit the 2^31-1 button id limit
	 */
	public synchronized int getNextHandlerId() {
		return handlerId++;
	}
}
