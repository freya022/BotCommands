package com.freya02.botcommands.buttons;

import com.freya02.botcommands.Logging;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.env.*;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a default implementation for a persistent component ID manager
 * <br>
 * <i>Uses Xodus as a server-less database</i>
 */
public class OldDefaultIdManager implements IdManager {
	private static final Logger LOGGER = Logging.getLogger();

	private final Environment env;
	private Store idStore;

	/**
	 * Creates a default ID manager for Discord components
	 *
	 * @param dbPath Path of the database folder, must exist
	 */
	public OldDefaultIdManager(Path dbPath) {
		if (Files.notExists(dbPath))
			throw new IllegalStateException("Path " + dbPath.toAbsolutePath() + " does not exist");

		if (!Files.isDirectory(dbPath))
			throw new IllegalStateException("Path " + dbPath.toAbsolutePath() + " is not a directory");

		env = Environments.newInstance(dbPath.toFile(), new EnvironmentConfig().setEnvCloseForcedly(true));
		Runtime.getRuntime().addShutdownHook(new Thread(env::close));

		env.executeInTransaction(txn -> idStore = env.openStore("ComponentIDs", StoreConfig.WITHOUT_DUPLICATES, txn));
	}

	/**
	 * Returns a unique value from a number
	 *
	 * @see <a href="https://wiki.postgresql.org/wiki/Pseudo_encrypt">PostgreSQL pseudo_encrypt</a>
	 */
	private static long pseudo_encrypt(long value) {
		long l1 = (value >> 16) & 0xffff;
		long r1 = value & 0xffff;

		long l2, r2;
		for (int i = 0; i < 3; i++) {
			l2 = r1;
			r2 = l1 ^ Math.round((((1366 * r1 + 150889) % 714025) / 714025.0) * 32767);
			l1 = l2;
			r1 = r2;
		}

		return (r1 << 16) + l1;
	}

	@Override
	public String getContent(String buttonId) {
		final String[] content = new String[1];

		env.executeInReadonlyTransaction(txn -> {
			final ByteIterable entry = idStore.get(txn, StringBinding.stringToEntry(buttonId));
			if (entry == null) {
				LOGGER.error("Button ID {} not found in database", buttonId);
				content[0] = null;
			} else {
				content[0] = StringBinding.entryToString(entry);
			}
		});

		return content[0];
	}

	@Override
	public String newId(String content) {
		final String[] buttonId = new String[1];
		env.executeInTransaction(txn -> {
			final long count = idStore.count(txn);
			buttonId[0] = String.valueOf(pseudo_encrypt(count));
			idStore.put(txn, StringBinding.stringToEntry(buttonId[0]), StringBinding.stringToEntry(content));
		});

		return buttonId[0];
	}
}
