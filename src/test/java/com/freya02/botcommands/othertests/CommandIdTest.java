package com.freya02.botcommands.othertests;

import com.freya02.botcommands.api.CommandStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommandIdTest {
	private record CmdPair(String path, String id) {}

	private static final Map<String, Long> commandIdToGuildsMap = new HashMap<>();
	private static final Map<String, String> commandIdToCommandPathMap = new HashMap<>();

//	private static class AllowedIds {
//		private final Map<Long, CmdPair> map = new HashMap<>();
//
//		public void put(long guildId, CmdPair allowedCommand) {
//			map.put(guildId, allowedCommand);
//		}
//
//		public CommandStatus get(long guildId, CmdPair command) {
//			return command.equals(map.get(guildId))
//					? CommandStatus.UNSURE
//					: CommandStatus.DISABLED;
//		}
//	}

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
	 * TODO
	 * ==> Given a command id, if it is bound to a guild, and it corresponds, then OK
	 * ==> Given a command id, if it is not bound to a guild then check if there is an ID using the same command paths **that is allowed in that guild**, if one exists then deny this one
	 */
	private static CommandStatus getStatus(String commandPath, String commandId, long guildId) {
		final String boundPath = commandIdToCommandPathMap.get(commandId);
		final Long allowedGuilds = commandIdToGuildsMap.get(commandId);

		if (!Objects.equals(allowedGuilds, guildId)) {
			return CommandStatus.DISABLED;
		}

		return CommandStatus.UNSURE;
	}

	public static void main(String[] args) {
		commandIdToCommandPathMap.put("specific_run", "specific");
		commandIdToCommandPathMap.put("global_run", "specific");

		commandIdToGuildsMap.put("specific_run", 123L);

		//If a command id has been marked as allowed in a guild then the commands with the same names must be disabled
		//Let's check for "specific_run" in guild 123, it should be allowed

		System.out.println(getStatus("specific", "specific_run", 123));
		System.out.println(getStatus("specific", "global_run", 123));
		System.out.println(getStatus("specific", "global_run", 1234));
	}
}
