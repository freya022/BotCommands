package com.freya02.botcommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class CommandListener extends ListenerAdapter {
	private final String prefix;
	private final Supplier<InputStream> defaultFooterIconSupplier;
	private final Supplier<EmbedBuilder> defaultEmbedFunction;
	private int prefixLength;

	private final List<Long> ownerIds;

	private final String userPermErrorMsg;
	private final String botPermErrorMsg;
	private final String commandNotFoundMsg;
	private final String ownerOnlyErrorMsg;
	private final String roleOnlyErrorMsg;

	private final String userCooldownMsg;
	private final String channelCooldownMsg;
	private final String guildCooldownMsg;

	private final boolean usePingAsPrefix;

	private final TreeMap<String, CommandInfo> stringCommandMap;

	private static final ThreadFactory threadFactory = runnable -> {
		final Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	};

	private static final ScheduledExecutorService userCooldownService = Executors.newSingleThreadScheduledExecutor(threadFactory);
	private static final ScheduledExecutorService channelCooldownService = Executors.newSingleThreadScheduledExecutor(threadFactory);
	private static final ScheduledExecutorService guildCooldownService = Executors.newSingleThreadScheduledExecutor(threadFactory);

	private static final Map<Long, ScheduledFuture<?>> userCooldowns = new HashMap<>();
	private static final Map<Long, ScheduledFuture<?>> channelCooldowns = new HashMap<>();
	private static final Map<Long, ScheduledFuture<?>> guildCooldowns = new HashMap<>();

	private String botId = null;
	private Pattern userPattern;

	public CommandListener(String prefix, List<Long> ownerIds, String userPermErrorMsg, String botPermErrorMsg, String commandNotFoundMsg, String ownerOnlyErrorMsg, String roleOnlyErrorMsg, String userCooldownMsg, String channelCooldownMsg, String guildCooldownMsg, boolean usePingAsPrefix, Supplier<EmbedBuilder> defaultEmbedFunction, Supplier<InputStream> defaultFooterIconSupplier, TreeMap<String, CommandInfo> stringCommandMap) {
		this.prefix = prefix;
		if (!usePingAsPrefix) {
			this.prefixLength = prefix.length();
		}

		this.ownerIds = ownerIds;
		this.userPermErrorMsg = userPermErrorMsg;
		this.botPermErrorMsg = botPermErrorMsg;
		this.commandNotFoundMsg = commandNotFoundMsg;
		this.ownerOnlyErrorMsg = ownerOnlyErrorMsg;
		this.roleOnlyErrorMsg = roleOnlyErrorMsg;
		this.userCooldownMsg = userCooldownMsg;
		this.channelCooldownMsg = channelCooldownMsg;
		this.guildCooldownMsg = guildCooldownMsg;
		this.usePingAsPrefix = usePingAsPrefix;
		this.defaultEmbedFunction = defaultEmbedFunction;
		this.defaultFooterIconSupplier = defaultFooterIconSupplier;
		this.stringCommandMap = stringCommandMap;
	}

	private final Object mutex = new Object();

	public Supplier<EmbedBuilder> getDefaultEmbedFunction() {
		return defaultEmbedFunction;
	}

	public Supplier<InputStream> getDefaultFooterIconSupplier() {
		return defaultFooterIconSupplier;
	}

	private static class Cmd {
		private String commandName, args;

		private Cmd() {}

		private Cmd(String commandName, String args) {
			this.commandName = commandName;
			this.args = args;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Cmd cmd = (Cmd) o;

			if (!Objects.equals(commandName, cmd.commandName)) return false;
			return Objects.equals(args, cmd.args);
		}

		@Override
		public int hashCode() {
			int result = commandName != null ? commandName.hashCode() : 0;
			result = 31 * result + (args != null ? args.hashCode() : 0);
			return result;
		}
	}

	/*private static Pattern pattern;
	private static Matcher matcher;
	private Cmd getCmdRegex(GuildMessageReceivedEvent event, String msg) {
		if (pattern == null) {
			if (usePingAsPrefix) {
				pattern = Pattern.compile("<@!?" + event.getJDA().getSelfUser().getId() + "> (\\S+)( \\S+.*)?");
			} else {
				pattern = Pattern.compile(prefix + "(\\S+)( \\S+.*)?");
			}

			matcher = pattern.matcher("");
		}

		matcher.reset(msg);
		if (matcher.find()) {
			final int groupCount = matcher.groupCount();
			final Cmd cmd = new Cmd();

			if (groupCount >= 1) {
				cmd.commandName = matcher.group(1);
			}

			if (groupCount >= 2) {
				final String group = matcher.group(2);
				if (group != null) {
					cmd.args = group.substring(1);
				} else cmd.args = "";
			} else {
				cmd.args = "";
			}

			return cmd;
		}

		return null;
	}*/

	private Cmd getCmdFast(GuildMessageReceivedEvent event, String msg) {
		final String msgNoPrefix;
		final String commandName;
		final String args;

		if (usePingAsPrefix) {
			if (botId == null) {
				botId = event.getJDA().getSelfUser().getId();
				userPattern = Pattern.compile("<@!?" + botId + ">");
			}

			if (!userPattern.matcher(msg).find())
				return null;

			final int i = msg.indexOf(' ');
			if (i == -1) {
				return null; //No command was issued
			}

			msgNoPrefix = msg.substring(i + 1);
		} else {
			if (!msg.startsWith(prefix)) {
				return null; //No prefix found
			}

			msgNoPrefix = msg.substring(prefixLength);
		}

		final int endIndex = msgNoPrefix.indexOf(' ');

		if (endIndex > -1) {
			commandName = msgNoPrefix.substring(0, endIndex);
		} else {
			commandName = msgNoPrefix;
		}

		args = msgNoPrefix.substring(commandName.length()).trim();

		return new Cmd(commandName, args);
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.getAuthor().isFake() || event.isWebhookMessage())
			return;

		final String msg = event.getMessage().getContentRaw();

		final Cmd cmdFast = getCmdFast(event, msg);
		//final Cmd cmdRegex = Benchmark.run("Regex", () -> getCmdRegex(event, msg)).getResult();

		if (cmdFast == null)
			return;

		/*if (!cmdFast.equals(cmdRegex))
			System.err.println("Fast != regex");*/

		final String commandName = cmdFast.commandName;
		String args = cmdFast.args;

		CommandInfo commandInfo = stringCommandMap.get(commandName);
		if (commandInfo == null) {
			reply(event, commandNotFoundMsg);
			return;
		}

		//Check for subcommands
		for (CommandInfo info : commandInfo.getSubcommandsInfo()) {
			if (args.startsWith(info.getName())) {
				commandInfo = info; //Replace command with subcommand
				args = args.replace(info.getName(), "").trim();
				break;
			}
		}

		final Member member = event.getMember();

		if (member == null) {
			System.err.println("Command caller member is null !");
			return;
		}

		final boolean isNotOwner = !ownerIds.contains(member.getIdLong());
		if (isNotOwner && commandInfo.isHidden()) {
			reply(event, commandNotFoundMsg);
			return;
		}

		if (isNotOwner && commandInfo.isRequireOwner()) {
			reply(event, ownerOnlyErrorMsg);
			return;
		}

		if (isNotOwner && !member.hasPermission(commandInfo.getUserPermissions())) {
			reply(event, userPermErrorMsg);
			return;
		}

		if (isNotOwner && !commandInfo.getRequiredRole().isEmpty()) {
			String requiredRole = commandInfo.getRequiredRole();

			boolean foundRole = false;
			for (Role role : member.getRoles()) {
				if (role.getName().equals(requiredRole)) {
					foundRole = true;
					break;
				}
			}

			if (!foundRole) {
				reply(event, String.format(roleOnlyErrorMsg, requiredRole));
				return;
			}
		}

		ScheduledFuture<?> cooldownFuture;
		if (commandInfo.getCooldownScope() == CooldownScope.USER) {
			if ((cooldownFuture = userCooldowns.get(member.getIdLong())) != null) {
				reply(event, String.format(userCooldownMsg, cooldownFuture.getDelay(TimeUnit.MILLISECONDS) / 1000.0));
				return;
			}
		} else if (commandInfo.getCooldownScope() == CooldownScope.GUILD) {
			if ((cooldownFuture = guildCooldowns.get(event.getGuild().getIdLong())) != null) {
				reply(event, String.format(guildCooldownMsg, cooldownFuture.getDelay(TimeUnit.MILLISECONDS) / 1000.0));
				return;
			}
		} else /*if (commandInfo.getCooldownScope() == CooldownScope.CHANNEL) {*/ //Implicit condition
			if ((cooldownFuture = channelCooldowns.get(event.getChannel().getIdLong())) != null) {
				reply(event, String.format(channelCooldownMsg, cooldownFuture.getDelay(TimeUnit.MILLISECONDS) / 1000.0));
				return;
			//}
		}

		if (!event.getGuild().getSelfMember().hasPermission(commandInfo.getBotPermissions())) {
			reply(event, botPermErrorMsg);
			return;
		}

		if (commandInfo.getCooldown() > 0) {
			if (commandInfo.getCooldownScope() == CooldownScope.USER) {
				startCooldown(userCooldowns, userCooldownService, member.getIdLong(), commandInfo.getCooldown());
			} else if (commandInfo.getCooldownScope() == CooldownScope.GUILD) {
				startCooldown(guildCooldowns, guildCooldownService, event.getGuild().getIdLong(), commandInfo.getCooldown());
			} else if (commandInfo.getCooldownScope() == CooldownScope.CHANNEL) {
				startCooldown(channelCooldowns, channelCooldownService, event.getChannel().getIdLong(), commandInfo.getCooldown());
			}
		}

		commandInfo.getCommand().execute(new CommandEvent(this, event, args));
	}

	public List<Long> getOwnerIds() {
		return ownerIds;
	}

	public CommandInfo getCommandInfo(String cmdName) {
		return stringCommandMap.get(cmdName);
	}

	private void startCooldown(Map<Long, ScheduledFuture<?>> cooldownMap, ScheduledExecutorService service, Long key, int cooldown) {
		final ScheduledFuture<?> future = service.schedule(() -> cooldownMap.remove(key), cooldown, TimeUnit.MILLISECONDS);

		cooldownMap.put(key, future);
	}

	private void reply(GuildMessageReceivedEvent event, String msg) {
		event.getChannel().sendMessage(msg).queue(null,
				e -> System.err.println("Could not send reply message from command listener because of " + e.getClass().getName() + " : " + e.getLocalizedMessage())
		);
	}
}