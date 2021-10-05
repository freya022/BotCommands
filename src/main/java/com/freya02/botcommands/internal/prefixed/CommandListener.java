package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.prefixed.MessageInfo;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.RunnableEx;
import com.freya02.botcommands.internal.Usability;
import com.freya02.botcommands.internal.Usability.UnusableReason;
import com.freya02.botcommands.internal.utils.Utils;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CommandListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
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

	@Nullable
	private String getMsgNoPrefix(String msg, Guild guild) {
		int prefixLength = -1;

		for (String prefix : getPrefixes(guild)) {
			if (msg.startsWith(prefix)) {
				prefixLength = prefix.length();
				break;
			}
		}

		if (prefixLength == -1) return null;

		return msg.substring(prefixLength).trim();
	}

	private List<String> getPrefixes(Guild guild) {
		final SettingsProvider settingsProvider = context.getSettingsProvider();

		if (settingsProvider != null) {
			final List<String> prefixes = settingsProvider.getPrefixes(guild);
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
			final String msgNoPrefix = getMsgNoPrefix(msg, event.getGuild());

			if (msgNoPrefix == null || msgNoPrefix.isBlank())
				return;

			LOGGER.trace("Received prefixed command: {}", msg);

			final String[] split = SPACE_PATTERN.split(msgNoPrefix, 4);
			final TextCommandCandidates candidates = getCommandCandidates(Arrays.copyOf(split, 3));

			final boolean isNotOwner = !context.isOwner(member.getIdLong());
			if (candidates == null) {
				onCommandNotFound(event, CommandPath.of(split[0]), isNotOwner);
				return;
			}

			final int count = candidates.findFirst().getPath().getNameCount();

			final String args = Arrays.stream(split)
					.skip(count) //Skip the number of path components as those are split and unneeded
					.collect(Collectors.joining(" "));

			for (TextCommandInfo candidate : candidates) {
				final Pattern pattern = candidate.getCompletePattern();

				if (pattern != null) { //Non-fallback, uses BaseCommandEvent
					final Matcher matcher = pattern.matcher(args);

					if (matcher.matches()) {
						tryExecute(event, member, isNotOwner, args, candidate, matcher);

						return;
					}
				} else { //Fallback, only CommandEvent
					tryExecute(event, member, isNotOwner, args, candidate, null);

					return;
				}
			}

			event.getChannel().sendMessage("help sent").queue();
		}, msg, event.getMessage());
	}

	private boolean tryExecute(GuildMessageReceivedEvent event, Member member, boolean isNotOwner, String args, TextCommandInfo candidate, Matcher matcher) throws Exception {
		final MessageInfo messageInfo = new MessageInfo(context, event, candidate, args);
		for (Predicate<MessageInfo> filter : context.getFilters()) {
			if (!filter.test(messageInfo)) {
				return false;
			}
		}

		final Usability usability = Usability.of(candidate, member, event.getChannel(), isNotOwner);

		if (usability.isUnusable()) {
			final var unusableReasons = usability.getUnusableReasons();
			if (unusableReasons.contains(UnusableReason.HIDDEN)) {
				onCommandNotFound(event, candidate.getPath(), true);
				return false;
			} else if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
				reply(event, this.context.getDefaultMessages(event.getGuild()).getOwnerOnlyErrorMsg());
				return false;
			} else if (unusableReasons.contains(UnusableReason.USER_PERMISSIONS)) {
				reply(event, this.context.getDefaultMessages(event.getGuild()).getUserPermErrorMsg());
				return false;
			} else if (unusableReasons.contains(UnusableReason.BOT_PERMISSIONS)) {
				final StringJoiner missingBuilder = new StringJoiner(", ");

				//Take needed permissions, extract bot current permissions
				final EnumSet<Permission> missingPerms = candidate.getBotPermissions();
				missingPerms.removeAll(event.getGuild().getSelfMember().getPermissions(event.getChannel()));

				for (Permission botPermission : missingPerms) {
					missingBuilder.add(botPermission.getName());
				}

				reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getBotPermErrorMsg(), missingBuilder));
				return false;
			}
		}

		if (isNotOwner) {
			final long cooldown = candidate.getCooldown(event);
			if (cooldown > 0) {
				if (candidate.getCooldownScope() == CooldownScope.USER) {
					reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getUserCooldownMsg(), cooldown / 1000.0));
					return false;
				} else if (candidate.getCooldownScope() == CooldownScope.GUILD) {
					reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getGuildCooldownMsg(), cooldown / 1000.0));
					return false;
				} else /*if (commandInfo.getCooldownScope() == CooldownScope.CHANNEL) {*/ //Implicit condition
					reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getChannelCooldownMsg(), cooldown / 1000.0));
				return false;
				//}
			}
		}

		candidate.applyCooldown(event);
		candidate.execute(context, event, args, matcher);

		return true;
	}

	@Nullable
	private TextCommandCandidates getCommandCandidates(String[] split) {
		final TextCommandCandidates commands = context.findCommands(CommandPath.of(split));
		if (commands != null) return commands;

		return getCommandCandidates(Arrays.copyOf(split, split.length - 1));
	}

	private void onCommandNotFound(GuildMessageReceivedEvent event, CommandPath commandName, boolean isNotOwner) {
		final List<String> suggestions = getSuggestions(event, commandName, isNotOwner);

		if (!suggestions.isEmpty()) {
			reply(event, String.format(context.getDefaultMessages(event.getGuild()).getCommandNotFoundMsg(), "**" + String.join("**, **", suggestions) + "**"));
		}
	}

	@NotNull
	private List<String> getSuggestions(GuildMessageReceivedEvent event, CommandPath triedCommandPath, boolean isNotOwner) {
		final Function<CommandPath, String> pathToStringFunc;

		switch (triedCommandPath.getNameCount()) {
			case 1:
				pathToStringFunc = CommandPath::getName;
				break;
			case 2:
			case 3:
				pathToStringFunc = CommandPath::getSubname;
				break;
			default:
				throw new IllegalStateException("Path empty or longer than 3 !");
		}

		final List<String> commandNames = context.getCommands().stream()
				.filter(c -> Usability.of(c.findFirst(), event.getMember(), event.getChannel(), isNotOwner).isUsable())
				.map(c -> pathToStringFunc.apply(c.findFirst().getPath()))
				.collect(Collectors.toList());

		final List<String> topPartial = FuzzySearch.extractAll(pathToStringFunc.apply(triedCommandPath),
				commandNames,
				FuzzySearch::partialRatio,
				90
		).stream().map(ExtractedResult::getString).collect(Collectors.toList());

		return FuzzySearch.extractTop(pathToStringFunc.apply(triedCommandPath),
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
			} catch (Throwable e) {
				e = Utils.getException(e);

				Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing request '" + msg + "'", e);

				message.addReaction(BaseCommandEventImpl.ERROR).queue();
				if (message.getTextChannel().canTalk()) {
					message.getChannel().sendMessage(context.getDefaultMessages(message.getGuild()).getCommandErrorMsg()).queue();
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