package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CommandStatus;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.utils.Utils;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandIdProcessor {
	private static final TLongSet EMPTY_LIST = new TLongHashSet();
	private final Map<String, TLongSet> commandIdToGuildsMap = new HashMap<>();
	private final Map<CommandPath, Set<String>> commandPathToCommandIdsMap = new HashMap<>();

	private final BContext context;

	public CommandIdProcessor(BContext context) {
		this.context = context;

		processCommandIds();
	}

	private void processCommandIds() {
		for (ApplicationCommandInfo info : context.getApplicationCommandsContext()
				.getApplicationCommandInfoMapView()
				.getAllApplicationCommandsView()) {
			final String commandId = info.getCommandId();

			if (commandId == null) continue;

			final CommandPath path = info.getPath();

			if (!commandPathToCommandIdsMap.computeIfAbsent(path, x -> new HashSet<>()).add(commandId)) {
				throw new IllegalArgumentException("Tried to insert a duplicated command ID '%s' to path '%s' (in %s)".formatted(
						commandId,
						path,
						Utils.formatMethodShort(info.getMethod())
				));
			}

			final Collection<Long> instanceAllowedGuilds = info.getInstance().getGuildsForCommandId(context, commandId, path);
			if (instanceAllowedGuilds != null) {
				commandIdToGuildsMap.computeIfAbsent(commandId, x -> new TLongHashSet()).addAll(instanceAllowedGuilds);
			}

			final SettingsProvider settingsProvider = context.getSettingsProvider();
			if (settingsProvider != null) {
				final Collection<Long> allowedGuilds = settingsProvider.getGuildsForCommandId(context, commandId, path);
				if (allowedGuilds != null) {
					commandIdToGuildsMap.computeIfAbsent(commandId, x -> new TLongHashSet()).addAll(allowedGuilds);
				}
			}
		}

		for (Map.Entry<CommandPath, Set<String>> entry : commandPathToCommandIdsMap.entrySet()) {
			final TLongObjectMap<String> usedGuildIdToCommandIdMap = new TLongObjectHashMap<>();

			for (String commandId : entry.getValue()) {
				final TLongSet set = commandIdToGuildsMap.get(commandId);
				if (set == null) continue;

				set.forEach(guildId -> {
					final String oldId = usedGuildIdToCommandIdMap.put(guildId, commandId);

					if (oldId != null) {
						throw new IllegalArgumentException("Guild ID %d has two commands IDs ('%s' and '%s') that share the same path '%s'".formatted(guildId, oldId, commandId, entry.getKey()));
					}

					return true;
				});
			}
		}
	}

	public CommandStatus getStatus(@NotNull CommandPath commandPath, @NotNull String commandId, long guildId) {
		//Given a command id, if it is bound to a guild, and it corresponds, then OK
		final TLongSet allowedGuilds = commandIdToGuildsMap.getOrDefault(commandId, EMPTY_LIST);

		//If the target command id is allowed in that guild then it means that the command is targeted toward the guild
		if (allowedGuilds.contains(guildId)) {
			return CommandStatus.UNSURE;
		} else if (allowedGuilds != EMPTY_LIST) {
			return CommandStatus.DISABLED;
		}

		//Given a command id, if it is not bound to a guild then check if there is an ID using the same command paths **that is allowed in that guild**, if one exists then deny this one
		final Set<String> relatedCommandIds = commandPathToCommandIdsMap.get(commandPath);
		if (relatedCommandIds != null) {
			for (String id : relatedCommandIds) {
				final TLongSet allowedGuildsOfRelated = commandIdToGuildsMap.getOrDefault(id, EMPTY_LIST);

				if (allowedGuildsOfRelated.contains(guildId)) {
					//Found a guild where the related command id is allowed, this means that the current command cannot be registered as this will cause duplicates / override the old one

					return CommandStatus.DISABLED;
				}
			}
		}

		//This happens in cases where the command id is not targeting a registered guild
		return CommandStatus.UNSURE;
	}
}
