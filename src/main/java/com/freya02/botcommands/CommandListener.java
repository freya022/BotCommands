package com.freya02.botcommands;

import com.freya02.botcommands.regex.ArgumentFunction;
import com.freya02.botcommands.regex.MethodPattern;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;

final class CommandListener extends ListenerAdapter {
	private final BContext context;

	private static final ScheduledExecutorService userCooldownService = Executors.newSingleThreadScheduledExecutor(Utils.createThreadFactory("User cooldown thread"));
	private static final ScheduledExecutorService channelCooldownService = Executors.newSingleThreadScheduledExecutor(Utils.createThreadFactory("Channel cooldown thread"));
	private static final ScheduledExecutorService guildCooldownService = Executors.newSingleThreadScheduledExecutor(Utils.createThreadFactory("Guild cooldown thread"));

	private static final Map<Long, ScheduledFuture<?>> userCooldowns = new HashMap<>();
	private static final Map<Long, ScheduledFuture<?>> channelCooldowns = new HashMap<>();
	private static final Map<Long, ScheduledFuture<?>> guildCooldowns = new HashMap<>();

	private int commandThreadNumber = 0;
	private final ExecutorService commandService = Executors.newCachedThreadPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> printExceptionString("An unexpected exception happened in command thread '" + t.getName() + "':", e));
		thread.setName("Command thread #" + commandThreadNumber++);

		return thread;
	});

	public CommandListener(BContext context) {
		this.context = context;
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
		for (String prefix : context.getPrefixes()) {
			if (msg.startsWith(prefix)) {
				prefixLength = prefix.length();
				break;
			}
		}
		if (prefixLength == -1) return null;

		msgNoPrefix = msg.substring(prefixLength).trim();

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

		if (context.isBlacklisted(event.getAuthor()) && !context.isOwner(event.getAuthor().getIdLong()))
			return;

		final Member member = event.getMember();

		if (member == null) {
			System.err.println("Command caller member is null !");
			return;
		}

		final String msg = event.getMessage().getContentRaw();

		final Cmd cmdFast = getCmdFast(msg);

		if (cmdFast == null)
			return;

		final String commandName = cmdFast.commandName;
		String args = cmdFast.args;

		Command command = context.findCommand(commandName);

		final List<Long> ownerIds = context.getOwnerIds();
		final boolean isNotOwner = !ownerIds.contains(member.getIdLong());
		if (command == null) {
			onCommandNotFound(event, commandName, isNotOwner);
			return;
		}

		//Check for subcommands
		final CommandInfo commandInfo = command.getInfo();
		for (Command subcommand : commandInfo.getSubcommands()) {
			final String subcommandName = subcommand.getInfo().getName();
			if (args.startsWith(subcommandName)) {
				command = subcommand; //Replace command with subcommand
				args = args.replace(subcommandName, "").trim();
				break;
			}
		}

		Command recurCmd = command;
		do {
			if (isNotOwner && recurCmd.getInfo().isHidden()) {
				onCommandNotFound(event, commandName, true);
				return;
			}

			if (isNotOwner && recurCmd.getInfo().isRequireOwner()) {
				reply(event, context.getDefaultMessages().getOwnerOnlyErrorMsg());
				return;
			}

			if (isNotOwner && !member.hasPermission(event.getChannel(), recurCmd.getInfo().getUserPermissions())) {
				reply(event, context.getDefaultMessages().getUserPermErrorMsg());
				return;
			}

			if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), recurCmd.getInfo().getBotPermissions())) {
				final EnumSet<Permission> permissions = event.getGuild().getSelfMember().getPermissions(event.getChannel());
				final StringJoiner missingBuilder = new StringJoiner(", ");
				for (Permission botPermission : recurCmd.getInfo().getBotPermissions()) {
					if (!permissions.contains(botPermission)) {
						missingBuilder.add(botPermission.getName());
					}
				}

				reply(event, String.format(context.getDefaultMessages().getBotPermErrorMsg(), missingBuilder.toString()));
				return;
			}
		} while ((recurCmd = recurCmd.getInfo().getParentCommand()) != null);

		if (isNotOwner) {
			ScheduledFuture<?> cooldownFuture;
			if (commandInfo.getCooldownScope() == CooldownScope.USER) {
				if ((cooldownFuture = userCooldowns.get(member.getIdLong())) != null) {
					reply(event, String.format(context.getDefaultMessages().getUserCooldownMsg(), cooldownFuture.getDelay(TimeUnit.MILLISECONDS) / 1000.0));
					return;
				}
			} else if (commandInfo.getCooldownScope() == CooldownScope.GUILD) {
				if ((cooldownFuture = guildCooldowns.get(event.getGuild().getIdLong())) != null) {
					reply(event, String.format(context.getDefaultMessages().getGuildCooldownMsg(), cooldownFuture.getDelay(TimeUnit.MILLISECONDS) / 1000.0));
					return;
				}
			} else /*if (commandInfo.getCooldownScope() == CooldownScope.CHANNEL) {*/ //Implicit condition
				if ((cooldownFuture = channelCooldowns.get(event.getChannel().getIdLong())) != null) {
					reply(event, String.format(context.getDefaultMessages().getChannelCooldownMsg(), cooldownFuture.getDelay(TimeUnit.MILLISECONDS) / 1000.0));
					return;
					//}
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
		}

		final Command finalCommand = command;
		for (MethodPattern m : finalCommand.getInfo().getMethodPatterns()) {
			try {
				final Matcher matcher = m.pattern.matcher(args);
				if (matcher.matches()) {
					final List<Object> objects = new ArrayList<>(matcher.groupCount());
					objects.add(new BaseCommandEventImpl(context, event, args));

					int groupIndex = 1;
					for (ArgumentFunction argumentFunction : m.argumentsArr) {
						final String[] groups = new String[argumentFunction.groups];
						for (int j = 0; j < argumentFunction.groups; j++) {
							groups[j] = matcher.group(groupIndex++);
						}

						objects.add(argumentFunction.function.solve(event, groups));
					}

					runCommand(() -> m.method.invoke(finalCommand, objects.toArray()), msg, event.getMessage());
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

		final CommandEvent commandEvent = new CommandEventImpl(context, event, args);
		runCommand(() -> finalCommand.execute(commandEvent), msg, event.getMessage());
	}

	private void onCommandNotFound(GuildMessageReceivedEvent event, String commandName, boolean isNotOwner) {
		List<String> suggestions = new ArrayList<>();

		for (Command otherCommand : ((BContextImpl) context).getCommands()) {
			String s = otherCommand.getInfo().getName();
			CommandInfo lCommandInfo = otherCommand.getInfo();

			if (isNotOwner && lCommandInfo.isHidden()) {
				continue;
			}

			if (isNotOwner && lCommandInfo.isRequireOwner()) {
				continue;
			}

			int i;
			for (i = 0; i < Math.min(s.length(), commandName.length()); i++) {
				if (s.charAt(i) != commandName.charAt(i)) break;
			}

			if (i > 1) {
				suggestions.add(s);
			}
		}

		if (!suggestions.isEmpty()) {
			reply(event, String.format(context.getDefaultMessages().getCommandNotFoundMsg(), "**" + String.join("**, **", suggestions) + "**"));
		}
	}

	private interface RunnableEx {
		void run() throws Exception;
	}

	static void printExceptionString(String message, Throwable e) {
		final CharArrayWriter out = new CharArrayWriter(1024);
		out.append(message).append("\n");
		final PrintWriter printWriter = new PrintWriter(out);
		e.printStackTrace(printWriter);
		System.err.println(out.toString());
	}

	private void runCommand(RunnableEx code, String msg, Message message) {
		commandService.submit(() -> {
			try {
				code.run();
			} catch (Exception e) {
				if (e instanceof InvocationTargetException) e = (Exception) e.getCause();
				printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing request '" + msg + "'", e);
				message.addReaction(BaseCommandEventImpl.ERROR).queue();
				if (((TextChannel) message.getChannel()).canTalk()) {
					message.getChannel().sendMessage("An uncaught exception occured").queue();
				}

				((BContextImpl) context).dispatchException(msg, e);
			}
		});
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