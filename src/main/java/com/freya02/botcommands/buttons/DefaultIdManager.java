package com.freya02.botcommands.buttons;

import com.freya02.botcommands.Logging;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.env.*;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
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
	private final Store idStore;

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
	}

	private static String random() {
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		final StringBuilder sb = new StringBuilder(64);
		for (int i = 0; i < 64; i++) {
			sb.append(chars[random.nextInt(0, chars.length)]);
		}

		return sb.toString();
	}

	@Override
	public String getContent(String buttonId) {
		return env.computeInReadonlyTransaction(txn -> {
			final ByteIterable entry = idStore.get(txn, StringBinding.stringToEntry(buttonId));
			if (entry == null) {
				LOGGER.error("Button ID {} not found in database", buttonId);
				return null;
			} else {
				return StringBinding.entryToString(entry);
			}
		});
	}

	@Override
	public String newId(String content) {
		return env.computeInTransaction(txn -> {
			ArrayByteIterable entry;
			String buttonId;

			do {
				buttonId = random();
			} while (idStore.get(txn, (entry = StringBinding.stringToEntry(buttonId))) != null);

			idStore.put(txn, entry, StringBinding.stringToEntry(content));

			return buttonId;
		});
	}

	@Override
	public void removeId(String buttonId) {
		env.executeInTransaction(txn -> {
			if (!idStore.delete(txn, StringBinding.stringToEntry(buttonId))) {
				LOGGER.warn("Tried to delete key '{}' but it was not in the Store", buttonId);
			}
		});
	}
}
