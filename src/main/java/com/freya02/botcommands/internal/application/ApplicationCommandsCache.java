package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.BContextImpl;
import com.google.gson.Gson;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ApplicationCommandsCache {
	private static final Logger LOGGER = Logging.getLogger();
	private final Path cachePath;

	ApplicationCommandsCache(BContextImpl context) throws IOException {
		cachePath = Path.of(System.getProperty("java.io.tmpdir"), context.getJDA().getSelfUser().getId() + "slashcommands");

		Files.createDirectories(cachePath);
	}

	public void deleteGuildCache(Guild guild) throws IOException {
		final Path path = getGuildCommandsPath(guild);

		Files.deleteIfExists(path);
	}

	static byte[] getCommandsBytes(Collection<CommandData> commandData) {
		DataArray json = DataArray.empty();
		json.addAll(commandData);

		return json.toJson();
	}

	static byte[] getPrivilegesBytes(Map<String, Collection<? extends CommandPrivilege>> cmdBaseNameToPrivilegesMap) {
		//Reference at net.dv8tion.jda.internal.entities.GuildImpl.updateCommandPrivileges
		//Except this time we bind permissions to base names
		DataArray array = DataArray.empty();
		cmdBaseNameToPrivilegesMap.forEach((cmdBaseName, list) -> {
			DataObject entry = DataObject.empty();
			entry.put("cmdBaseName", cmdBaseName);
			entry.put("permissions", DataArray.fromCollection(list));
			array.add(entry);
		});

		return array.toJson();
	}

	public static boolean isJsonContentSame(byte[] oldContentBytes, byte[] newContentBytes) {
		final String oldContent = new String(oldContentBytes);
		final String newContent = new String(newContentBytes);

		final Object oldMap = new Gson().fromJson(oldContent, Object.class);
		final Object newMap = new Gson().fromJson(newContent, Object.class);

		return checkDiff(oldMap, newMap);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	private static boolean checkDiff(Object oldObj, Object newObj) {
		if (oldObj.getClass() != newObj.getClass()) {
			LOGGER.trace("Class type not equal: {} to {}", oldObj.getClass().getSimpleName(), newObj.getClass().getSimpleName());

			return false;
		}

		if (oldObj instanceof Map<?, ?> oldMap && newObj instanceof Map<?, ?> newMap) {
			if (!oldMap.keySet().containsAll(newMap.keySet())) return false;

			for (Object key : oldMap.keySet()) {
				if (!checkDiff(oldMap.get(key), newMap.get(key))) {
					LOGGER.trace("Map value not equal for key '{}': {} to {}", key, oldMap.get(key), newMap.get(key));

					return false;
				}
			}
		} else if (oldObj instanceof List<?> oldList && newObj instanceof List<?> newList) {
			if (oldList.size() != newList.size()) return false;

			for (int i = 0; i < oldList.size(); i++) {
				final int index = newList.indexOf(oldList.get(i));
				if (index > -1) {
					//Not a change - don't log it
//					LOGGER.trace("Found exact object at index {} (original object at {}) : {}", index, i, oldList.get(i));

					continue;
				}

				if (!checkDiff(oldList.get(i), newList.get(i))) {
					LOGGER.trace("List item not equal: {} to {}", oldList.get(i), newList.get(i));

					return false;
				}
			}
		} else {
			final boolean equals = oldObj.equals(newObj);

			if (!equals) LOGGER.trace("Not same object: {} to {}", oldObj, newObj);

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

	Path getGuildPrivilegesPath(Guild guild) {
		return cachePath.resolve(guild.getId()).resolve("privileges.json");
	}
}