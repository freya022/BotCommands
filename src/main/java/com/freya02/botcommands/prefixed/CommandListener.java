package com.freya02.botcommands.prefixed;

import com.freya02.botcommands.*;
import com.freya02.botcommands.Usability.UnusableReason;
import com.freya02.botcommands.prefixed.impl.BaseCommandEventImpl;
import com.freya02.botcommands.prefixed.impl.CommandEventImpl;
import com.freya02.botcommands.prefixed.regex.ArgumentFunction;
import com.freya02.botcommands.prefixed.regex.MethodPattern;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public final class CommandListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;

	private int commandThreadNumber = 0;
	private final ExecutorService commandService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in command thread '" + t.getName() + "':", e));
		thread.setName("Command thread #" + commandThreadNumber++);

		return thread;
	});

	public CommandListener(BContextImpl context) {
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

	private Cmd getCmdFast(String msg, long guildId) {
		final String msgNoPrefix;
		final String commandName;

		int prefixLength = -1;
		for (String prefix : getPrefixes(guildId)) {
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

	private List<String> getPrefixes(long guildId) {
		final BGuildSettings guildSettings = context.getGuildSettings(guildId);

		if (guildSettings != null) {
			final List<String> prefixes = guildSettings.getPrefixes();
			if (prefixes == null || prefixes.isEmpty()) return context.getPrefixes();

			return prefixes;
		} else {
			return context.getPrefixes();
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.isWebhookMessage())
			return;

		final Member member = event.getMember();

		if (member == null) {
			LOGGER.error("Command caller member is null ! This shouldn't happen if the message isn't a webhook, or is the docs wrong ?");
			return;
		}

		final String msg = event.getMessage().getContentRaw();
		runCommand(() -> {
			final Cmd cmdFast = getCmdFast(msg, event.getGuild().getIdLong());

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

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Received prefixed command: {}", msg);
			}

			final MessageInfo messageInfo = new MessageInfo(context, event, command, args);
			for (Predicate<MessageInfo> filter : context.getFilters()) {
				if (!filter.test(messageInfo)) {
					return;
				}
			}

			final Usability usability = Usability.of(command.getInfo(), member, event.getChannel(), isNotOwner);

			if (usability.isUnusable()) {
				final var unusableReasons = usability.getUnusableReasons();
				if (unusableReasons.contains(UnusableReason.HIDDEN)) {
					onCommandNotFound(event, commandName, true);
					return;
				} else if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
					reply(event, this.context.getDefaultMessages().getOwnerOnlyErrorMsg());
					return;
				} else if (unusableReasons.contains(UnusableReason.USER_PERMISSIONS)) {
					reply(event, this.context.getDefaultMessages().getUserPermErrorMsg());
					return;
				} else if (unusableReasons.contains(UnusableReason.BOT_PERMISSIONS)) {
					final StringJoiner missingBuilder = new StringJoiner(", ");

					//Take needed permissions, extract bot current permissions
					final EnumSet<Permission> missingPerms = command.getInfo().getBotPermissions();
					missingPerms.removeAll(event.getGuild().getSelfMember().getPermissions(event.getChannel()));

					for (Permission botPermission : missingPerms) {
						missingBuilder.add(botPermission.getName());
					}

					reply(event, String.format(this.context.getDefaultMessages().getBotPermErrorMsg(), missingBuilder));
					return;
				}
			}

			if (isNotOwner) {
				final int cooldown = command.getInfo().getCooldown(event);
				if (cooldown > 0) {
					if (commandInfo.getCooldownScope() == CooldownScope.USER) {
						reply(event, String.format(this.context.getDefaultMessages().getUserCooldownMsg(), cooldown / 1000.0));
						return;
					} else if (commandInfo.getCooldownScope() == CooldownScope.GUILD) {
						reply(event, String.format(this.context.getDefaultMessages().getGuildCooldownMsg(), cooldown / 1000.0));
						return;
					} else /*if (commandInfo.getCooldownScope() == CooldownScope.CHANNEL) {*/ //Implicit condition
						reply(event, String.format(this.context.getDefaultMessages().getChannelCooldownMsg(), cooldown / 1000.0));
						return;
					//}
				}
			}

			final Command finalCommand = command;
			patternsLoop: //mmmmmhhh spaghetti
			for (MethodPattern m : finalCommand.getInfo().getMethodPatterns()) {
				try {
					final Matcher matcher = m.pattern.matcher(args);
					if (matcher.matches()) {
						final List<Object> objects = new ArrayList<>(matcher.groupCount() + 1);
						objects.add(new BaseCommandEventImpl(this.context, event, args));

						int groupIndex = 1;
						for (ArgumentFunction argumentFunction : m.argumentsArr) {
							final String[] groups = new String[argumentFunction.groups];
							for (int j = 0; j < argumentFunction.groups; j++) {
								groups[j] = matcher.group(groupIndex++);
							}

							//For some reason using an array list instead of a regular array
							// magically unboxes primitives when passed to Method#invoke
							final Object o = argumentFunction.resolver.resolve(event, groups);
							if (o == null) {
								continue patternsLoop;
							}
							objects.add(o);
						}

						command.getInfo().applyCooldown(event);
						m.method.invoke(finalCommand, objects.toArray());
						return;
					}
				} catch (NumberFormatException e) {
					LOGGER.error("Invalid number");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			final CommandEvent commandEvent = new CommandEventImpl(this.context, event, args);
			command.getInfo().applyCooldown(event);
			finalCommand.execute(commandEvent);
		}, msg, event.getMessage());
	}

	private void onCommandNotFound(GuildMessageReceivedEvent event, String commandName, boolean isNotOwner) {
		final List<String> suggestions = getSuggestions(event, commandName, isNotOwner);

		if (!suggestions.isEmpty()) {
			reply(event, String.format(context.getDefaultMessages().getCommandNotFoundMsg(), "**" + String.join("**, **", suggestions) + "**"));
		}
	}

	@Nonnull
	private List<String> getSuggestions(GuildMessageReceivedEvent event, String commandName, boolean isNotOwner) {
		final List<String> commandNames = context.getCommands().stream()
				.filter(c -> Usability.of(c.getInfo(), event.getMember(), event.getChannel(), isNotOwner).isUsable())
				.map(c -> c.getInfo().getName())
				.collect(Collectors.toList());

		final List<String> topPartial = FuzzySearch.extractAll(commandName,
				commandNames,
				FuzzySearch::partialRatio,
				90
		).stream().map(ExtractedResult::getString).collect(Collectors.toList());

		return FuzzySearch.extractTop(commandName,
				topPartial,
				FuzzySearch::ratio,
				5,
				42
		).stream().map(ExtractedResult::getString).collect(Collectors.toList());
	}

	private void runCommand(RunnableEx code, String msg, Message message) {
		commandService.execute(() -> {
			try {
				code.run();
			} catch (Exception e) {
				e = Utils.getException(e);

				Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing request '" + msg + "'", e);

				message.addReaction(BaseCommandEventImpl.ERROR).queue();
				if (((TextChannel) message.getChannel()).canTalk()) {
					message.getChannel().sendMessage("An uncaught exception occurred").queue();
				}

				context.dispatchException(msg, e);
			}
		});
	}

	private void reply(GuildMessageReceivedEvent event, String msg) {
		final RestAction<? extends MessageChannel> channelAction;

		if (event.getChannel().canTalk()) {
			channelAction = new CompletedRestAction<>(event.getJDA(), event.getChannel());
		} else {
			channelAction = event.getAuthor().openPrivateChannel();
		}

		channelAction.queue(channel -> channel.sendMessage(msg)
				.queue(null,
						new ErrorHandler()
								.ignore(ErrorResponse.CANNOT_SEND_TO_USER)
								.handle(Exception.class, e -> {
									Utils.printExceptionString("Could not send reply message from command listener", e);

									context.dispatchException("Could not send reply message from command listener", e);
								})
				));
	}
}