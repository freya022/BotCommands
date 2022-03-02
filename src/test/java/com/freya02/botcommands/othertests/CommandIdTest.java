package com.freya02.botcommands.othertests;

import com.freya02.botcommands.api.CommandStatus;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandIdTest {
	private static final TLongArrayList EMPTY_LIST = new TLongArrayList();
	private static final Map<String, TLongList> commandIdToGuildsMap = new HashMap<>();
	private static final Map<String, List<String>> commandPathToCommandIdsMap = new HashMap<>();

	/* Command path   Command ID   Guild ID
	 * specific       guild_run    123
	 * specific       global_run
	 *
	 * Trying to use guild_run   in guild 123: OK
	 * Trying to use guild_run   in guild 000: not OK
	 * Trying to use global_run  in guild 123: not OK
	 * Trying to use global_run  in guild 000: OK
	 * Trying to use foobar      in guild 123: OK
	 * Trying to use foobar      in guild 000: OK
	 *
	 * Test for guild_run and guild 123:
	 *   The queried guild id corresponds to the target guild id
	 *
	 * Test for guild_run and guild 000:
	 *   The queried guild doesn't exist, cannot check to target guild id
	 *
	 * If a command id has been marked as allowed in a guild then the commands with the same names must be disabled
	 *
	 * ==> Given a command id, if it is bound to a guild, and it corresponds, then OK
	 * ==> Given a command id, if it is not bound to a guild then check if there is an ID using the same command paths **that is allowed in that guild**, if one exists then deny this one
	 */
	private static CommandStatus getStatus(String commandPath, String commandId, long guildId) {
		//Given a command id, if it is bound to a guild, and it corresponds, then OK
		final TLongList allowedGuilds = commandIdToGuildsMap.getOrDefault(commandId, EMPTY_LIST);

		//If the target command id is allowed in that guild then it means that the command is targeted toward the guild
		if (allowedGuilds.contains(guildId)) {
			return CommandStatus.UNSURE;
		}

		//Given a command id, if it is not bound to a guild then check if there is an ID using the same command paths **that is allowed in that guild**, if one exists then deny this one
		final List<String> relatedCommandIds = commandPathToCommandIdsMap.get(commandPath);
		for (String id : relatedCommandIds) {
			final TLongList allowedGuildsOfRelated = commandIdToGuildsMap.getOrDefault(id, EMPTY_LIST);

			if (allowedGuildsOfRelated.contains(guildId)) {
				//Found a guild where the related command id is allowed, this means that the current command cannot be registered as this will cause duplicates / override the old one

				return CommandStatus.DISABLED;
			}
		}

		//This happens in cases where the command id is not targeting a registered guild
		return CommandStatus.UNSURE;
	}

	public static void main(String[] args) {
		commandPathToCommandIdsMap.computeIfAbsent("specific", x -> new ArrayList<>()).add("specific_run");
		commandPathToCommandIdsMap.computeIfAbsent("specific", x -> new ArrayList<>()).add("global_run");

		commandIdToGuildsMap.put("specific_run", TLongArrayList.wrap(new long[]{123L}));

		//If a command id has been marked as allowed in a guild then the commands with the same names must be disabled
		//Let's check for "specific_run" in guild 123, it should be allowed

		System.out.println(getStatus("specific", "specific_run", 123));
		System.out.println(getStatus("specific", "global_run", 123));
		System.out.println(getStatus("specific", "sth_run", 123));
		System.out.println(getStatus("specific", "global_run", 1234));
		System.out.println(getStatus("specific", "specific_run", 1234));
	}
}
