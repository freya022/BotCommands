package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.internal.application.diff.DiffLogger;
import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.data.DataArray;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ApplicationCommandsCache {
	private final Path cachePath;

	public ApplicationCommandsCache(@NotNull JDA jda) throws IOException {
		cachePath = Path.of(System.getProperty("java.io.tmpdir"), jda.getSelfUser().getId() + "slashcommands");

		Files.createDirectories(cachePath);
	}

	static byte[] getCommandsBytes(Collection<CommandData> commandData) {
		DataArray json = DataArray.empty();
		json.addAll(commandData);

		return json.toJson();
	}

	public static boolean isJsonContentSame(byte[] oldContentBytes, byte[] newContentBytes) {
		final String oldContent = new String(oldContentBytes);
		final String newContent = new String(newContentBytes);

		final Object oldMap = new Gson().fromJson(oldContent, Object.class);
		final Object newMap = new Gson().fromJson(newContent, Object.class);

		final DiffLogger diffLogger = DiffLogger.getLogger();

		final boolean isSame = checkDiff(oldMap, newMap, diffLogger, 0);

		diffLogger.printLogs();

		return isSame;
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	private static boolean checkDiff(Object oldObj, Object newObj, DiffLogger logger, int indent) {
		if (oldObj == null) {
			logger.trace(indent, "oldObj is null");

			return false;
		} else if (newObj == null) {
			logger.trace(indent, "newObj is null");

			return false;
		}

		if (oldObj.getClass() != newObj.getClass()) {
			logger.trace(indent, "Class type not equal: %s to %s", oldObj.getClass().getSimpleName(), newObj.getClass().getSimpleName());

			return false;
		}

		if (oldObj instanceof Map<?, ?> oldMap && newObj instanceof Map<?, ?> newMap) {
			if (!oldMap.keySet().containsAll(newMap.keySet())) return false;

			for (Object key : oldMap.keySet()) {
				if (!checkDiff(oldMap.get(key), newMap.get(key), logger, indent + 1)) {
					logger.trace(indent, "Map value not equal for key '%s': %s to %s", key, oldMap.get(key), newMap.get(key));

					return false;
				}
			}
		} else if (oldObj instanceof List<?> oldList && newObj instanceof List<?> newList) {
			if (oldList.size() != newList.size()) return false;

			for (int i = 0; i < oldList.size(); i++) {
				boolean found = false;
				int index = -1;

				for (Object o : newList) {
					index++;

					if (checkDiff(oldList.get(i), o, logger, indent + 1)) {
						found = true;
						break;
					}
				}

				if (found) {
					//If command options (parameters, not subcommands, not groups) are moved
					// then it means the command data changed
					if (i != index) {
						//Check if any final command property is here,
						// such as autocomplete, or required
						if (oldList.get(index) instanceof Map<?, ?> map
								&& map.get("autocomplete") != null) {
							//We found a real command option that has **changed index**,
							// this is NOT equal under different indexes

							logger.trace(indent, "Final command option has changed place from index %s to %s : %s", i, index, oldList.get(i));

							return false;
						}
					}

					logger.trace(indent, "Found exact object at index %s (original object at %s) : %s", index, i, oldList.get(i));

					continue;
				}

				if (!checkDiff(oldList.get(i), newList.get(i), logger, indent + 1)) {
					logger.trace(indent,"List item not equal: %s to %s", oldList.get(i), newList.get(i));

					return false;
				}
			}
		} else {
			final boolean equals = oldObj.equals(newObj);

			if (!equals) logger.trace(indent,"Not same object: %s to %s", oldObj, newObj);

			return equals;
		}

		return true;
	}

	Path getGlobalCommandsPath() {
		return cachePath.resolve("globalCommands.json");
	}

	Path getGuildCommandsPath(Guild guild) {
		return cachePath.resolve(guild.getId()).resolve("commands.json");
	}
}