package com.freya02.botcommands;

import com.freya02.botcommands.regex.ArgumentFunction;
import com.freya02.botcommands.regex.MethodPattern;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;

final class CommandListener extends ListenerAdapter {
	private final List<String> prefixes;
	private final Supplier<InputStream> defaultFooterIconSupplier;
	private final Supplier<EmbedBuilder> defaultEmbedFunction;

	private final List<Long> ownerIds;

	private final String userPermErrorMsg;
	private final String botPermErrorMsg;
	private final String commandNotFoundMsg;
	private final String ownerOnlyErrorMsg;
	private final String roleOnlyErrorMsg;

	private final String userCooldownMsg;
	private final String channelCooldownMsg;
	private final String guildCooldownMsg;

	private final Map<String, CommandInfo> stringCommandMap;
	private final String commandDisabledMsg;
	private final List<String> disabledCommands;

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

	private final ExecutorService commandService = Executors.newCachedThreadPool();

	public CommandListener(List<String> prefixes, List<Long> ownerIds, String userPermErrorMsg, String botPermErrorMsg, String commandNotFoundMsg, String commandDisabledMsg, String ownerOnlyErrorMsg, String roleOnlyErrorMsg, String userCooldownMsg, String channelCooldownMsg, String guildCooldownMsg, Supplier<EmbedBuilder> defaultEmbedFunction, Supplier<InputStream> defaultFooterIconSupplier, Map<String, CommandInfo> stringCommandMap, List<String> disabledCommands) {
		this.prefixes = prefixes;
		this.disabledCommands = disabledCommands;

		this.ownerIds = Collections.unmodifiableList(ownerIds);
		this.userPermErrorMsg = userPermErrorMsg;
		this.botPermErrorMsg = botPermErrorMsg;
		this.commandNotFoundMsg = commandNotFoundMsg;
		this.commandDisabledMsg = commandDisabledMsg;
		this.ownerOnlyErrorMsg = ownerOnlyErrorMsg;
		this.roleOnlyErrorMsg = roleOnlyErrorMsg;
		this.userCooldownMsg = userCooldownMsg;
		this.channelCooldownMsg = channelCooldownMsg;
		this.guildCooldownMsg = guildCooldownMsg;
		this.defaultEmbedFunction = defaultEmbedFunction;
		this.defaultFooterIconSupplier = defaultFooterIconSupplier;
		this.stringCommandMap = stringCommandMap;
	}

	public Supplier<EmbedBuilder> getDefaultEmbedFunction() {
		return defaultEmbedFunction;
	}

	public Supplier<InputStream> getDefaultFooterIconSupplier() {
		return defaultFooterIconSupplier;
	}

	private static class Cmd {
		private final String commandName;
		private final String args;

		private Cmd(String commandName, String args) {
			this.commandName = commandName;
			this.args = args;
		}
	}

	private Cmd getCmdFast(String msg) {
		final String msgNoPrefix;
		final String commandName;

		int prefixLength = -1;
		for (String prefix : prefixes) {
			if (msg.startsWith(prefix)) {
				prefixLength = prefix.length();
				break;
			}
		}
		if (prefixLength == -1) return null;

		msgNoPrefix = msg.substring(prefixLength);

		final int endIndex = msgNoPrefix.indexOf(' ');

		if (endIndex > -1) {
			commandName = msgNoPrefix.substring(0, endIndex);
			return new Cmd(commandName, msgNoPrefix.substring(commandName.length()).trim());
		} else {
			return new Cmd(msgNoPrefix, "");
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.isWebhookMessage())
			return;

		final String msg = event.getMessage().getContentRaw();

		final Cmd cmdFast = getCmdFast(msg);

		if (cmdFast == null)
			return;

		final String commandName = cmdFast.commandName;
		String args = cmdFast.args;

		CommandInfo commandInfo = stringCommandMap.get(commandName);

		if (commandInfo == null) {
			if (disabledCommands.contains(commandName)) {
				reply(event, commandDisabledMsg);
			} else {
				List<String> suggestions = new ArrayList<>();
				for (String s : stringCommandMap.keySet()) {
					int i;
					for (i = 0; i < Math.min(s.length(), commandName.length()); i++) {
						if (s.charAt(i) != commandName.charAt(i)) break;
					}

					if (i > 1) {
						suggestions.add(s);
					}
				}

				if (!suggestions.isEmpty()) {
					reply(event, String.format(commandNotFoundMsg, "**" + String.join("**, **", suggestions) + "**"));
				}
			}
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

		//Check for disabled subcommands
		final int i = args.indexOf(' ', 1);
		final String subcommandName = i == -1 ? args : args.substring(0, i);
		if (disabledCommands.contains(commandInfo.getName() + "." + subcommandName)) {
			reply(event, commandDisabledMsg);
			return;
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

		CommandInfo finalCommandInfo = commandInfo;
		for (MethodPattern m : commandInfo.getMethodPatterns()) {
			try {
				final Matcher matcher = m.pattern.matcher(args);
				if (matcher.find()) {
					final List<Object> objects = new ArrayList<>(matcher.groupCount());
					objects.add(new BaseCommandEvent(this, event, commandName, args));

					int groupIndex = 1;
					for (ArgumentFunction argumentFunction : m.argumentsArr) {
						final String[] groups = new String[argumentFunction.groups];
						for (int j = 0; j < argumentFunction.groups; j++) {
							groups[j] = matcher.group(groupIndex++);
						}

						objects.add(argumentFunction.function.solve(event, groups));
					}

					runCommand(() -> m.method.invoke(finalCommandInfo.getCommand(), objects.toArray()), msg);
					return;
				}
			} catch (NumberFormatException e) {
				System.err.println("Invalid number");
			} catch (ErrorResponseException | NoSuchElementException e) {
				//might be normal
			}  catch (Exception e) {
				e.printStackTrace();
			}
		}

		final CommandEvent commandEvent = new CommandEvent(this, event, commandName, args);
		runCommand(() -> finalCommandInfo.getCommand().execute(commandEvent), msg);
	}

	private interface RunnableEx {
		void run() throws Exception;
	}

	private void runCommand(RunnableEx code, String msg) {
		commandService.submit(() -> {
			try {
				code.run();
			} catch (Exception e) {
				final CharArrayWriter out = new CharArrayWriter(512);
				out.append("Unhandled exception in thread '").append(Thread.currentThread().getName()).append("' while executing request '").append(msg).append("'\n");
				final PrintWriter printWriter = new PrintWriter(out);
				e.printStackTrace(printWriter);
				System.err.println(out.toString());
			}
		});
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