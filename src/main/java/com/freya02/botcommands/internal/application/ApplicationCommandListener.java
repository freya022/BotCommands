package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.DefaultMessages;
import com.freya02.botcommands.api.ExceptionHandler;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.ApplicationCommandFilter;
import com.freya02.botcommands.api.application.ApplicationFilteringData;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.RunnableEx;
import com.freya02.botcommands.internal.Usability;
import com.freya02.botcommands.internal.Usability.UnusableReason;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ApplicationCommandListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;

	private int commandThreadNumber = 0;
	private final ExecutorService commandService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in an application command thread '" + t.getName() + "':", e));
		thread.setName("Application command thread #" + commandThreadNumber++);

		return thread;
	});

	public ApplicationCommandListener(BContextImpl context) {
		this.context = context;
	}

	@SubscribeEvent
	@Override
	public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
		LOGGER.trace("Received user command: {}", event.getName());

		final Consumer<Throwable> throwableConsumer = getThrowableConsumer(event);
		runCommand(() -> {
			final UserCommandInfo userCommand = context.getApplicationCommandsContext().findLiveUserCommand(event.getGuild(), event.getName());

			if (userCommand == null) {
				throwableConsumer.accept(new IllegalArgumentException("An user context command could not be found: " + event.getName()));
				printAvailableCommands(event);
				return;
			}

			if (!canRun(event, userCommand)) return;

			userCommand.execute(context, event, throwableConsumer);
		}, throwableConsumer);
	}

	@SubscribeEvent
	@Override
	public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
		LOGGER.trace("Received message command: {}", event.getName());

		final Consumer<Throwable> throwableConsumer = getThrowableConsumer(event);
		runCommand(() -> {
			final MessageCommandInfo messageCommand = context.getApplicationCommandsContext().findLiveMessageCommand(event.getGuild(), event.getName());

			if (messageCommand == null) {
				throwableConsumer.accept(new IllegalArgumentException("A message context command could not be found: " + event.getName()));
				printAvailableCommands(event);
				return;
			}

			if (!canRun(event, messageCommand)) return;

			messageCommand.execute(context, event, throwableConsumer);
		}, throwableConsumer);
	}

	@SubscribeEvent
	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		LOGGER.trace("Received slash command: {}", reconstructCommand(event));

		final Consumer<Throwable> throwableConsumer = getThrowableConsumer(event);
		runCommand(() -> {
			final SlashCommandInfo slashCommand = context.getApplicationCommandsContext().findLiveSlashCommand(event.getGuild(), CommandPath.of(event.getFullCommandName()));

			if (slashCommand == null) {
				throwableConsumer.accept(new IllegalArgumentException("A slash command could not be found: '" + event.getFullCommandName() + "'"));
				printAvailableCommands(event);
				return;
			}

			if (!canRun(event, slashCommand)) return;

			slashCommand.execute(context, event, throwableConsumer);
		}, throwableConsumer);
	}

	private void printAvailableCommands(@NotNull GenericCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		if (LOGGER.isTraceEnabled()) {
			final var commandsMap = context.getApplicationCommandsContext().getLiveApplicationCommandsMap(guild);
			final var scopeName = guild != null ? "'" + guild.getName() + "'" : "Global scope";
			LOGGER.trace("Commands available in {}: {}", scopeName, commandsMap.getAllApplicationCommandsStream()
					.map(commandInfo -> "/" + commandInfo.getPath().getFullPath())
					.sorted()
					.collect(Collectors.joining("\n"))
			);
		}
		if (context.isOnlineAppCommandCheckEnabled()) {
			LOGGER.warn("""
					An application command could not be recognized even though online command check was performed. An update will be forced.
					Please check if you have another bot instance running as it could have replaced the current command set.
					Do not share your tokens with anyone else (even your friend), and use a separate token when testing.""");
			if (guild != null) {
				context.scheduleApplicationCommandsUpdate(guild, true, true).whenComplete((wasUpdated, e) -> {
					if (e != null)
						LOGGER.error("An exception occurred while trying to update commands of guild '{}' ({}) after a command was missing", guild.getName(), guild.getId(), e);
				});
			} else {
				context.scheduleGlobalApplicationCommandsUpdate(true, true).whenComplete((wasUpdated, e) -> {
					if (e != null)
						LOGGER.error("An exception occurred while trying to update global commands after a command was missing", e);
				});
			}
		}
	}

	@NotNull
	public static String reconstructCommand(GenericCommandInteractionEvent event) {
		if (event instanceof SlashCommandInteractionEvent slashEvent) {
			return slashEvent.getCommandString();
		} else {
			return event.getName();
		}
	}

	private boolean canRun(@NotNull GenericCommandInteractionEvent event, ApplicationCommandInfo applicationCommand) {
		for (ApplicationCommandFilter applicationFilter : context.getApplicationFilters()) {
			if (!applicationFilter.isAccepted(new ApplicationFilteringData(context, event, applicationCommand))) {
				LOGGER.trace("Cancelled application commands due to filter");

				return false;
			}
		}

		final boolean isNotOwner = !context.isOwner(event.getUser().getIdLong());
		final Usability usability = Usability.of(event, applicationCommand, isNotOwner);

		if (usability.isUnusable()) {
			final var unusableReasons = usability.getUnusableReasons();
			if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
				reply(event, this.context.getDefaultMessages(event).getOwnerOnlyErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.NSFW_DISABLED)) {
				reply(event, this.context.getDefaultMessages(event).getNsfwDisabledErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.NSFW_ONLY)) {
				reply(event, this.context.getDefaultMessages(event).getNSFWOnlyErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.NSFW_DM_DENIED)) {
				reply(event, this.context.getDefaultMessages(event).getNSFWDMDeniedErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.USER_PERMISSIONS)) {
				reply(event, this.context.getDefaultMessages(event).getUserPermErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.BOT_PERMISSIONS)) {
				if (event.getGuild() == null) throw new IllegalStateException("BOT_PERMISSIONS got checked even if guild is null");

				final StringJoiner missingBuilder = new StringJoiner(", ");

				//Take needed permissions, extract bot current permissions
				final EnumSet<Permission> missingPerms = applicationCommand.getBotPermissions();
				missingPerms.removeAll(event.getGuild().getSelfMember().getPermissions(event.getGuildChannel()));

				for (Permission botPermission : missingPerms) {
					missingBuilder.add(botPermission.getName());
				}

				reply(event, this.context.getDefaultMessages(event).getBotPermErrorMsg(missingBuilder.toString()));

				return false;
			}
		}

		if (isNotOwner && applicationCommand.getCooldownMillis() > 0) {
			final long cooldown = applicationCommand.getCooldown(event, event::getFullCommandName);
			if (cooldown > 0) {
				final DefaultMessages messages = this.context.getDefaultMessages(event);
				if (applicationCommand.getCooldownScope() == CooldownScope.USER) {
					reply(event, messages.getUserCooldownMsg(cooldown / 1000.0));
				} else if (applicationCommand.getCooldownScope() == CooldownScope.GUILD) {
					reply(event, messages.getGuildCooldownMsg(cooldown / 1000.0));
				} else { //Implicit channel
					reply(event, messages.getChannelCooldownMsg(cooldown / 1000.0));
				}

				return false;
			}
		}
		
		return true;
	}

	private void runCommand(RunnableEx code, Consumer<Throwable> throwableConsumer) {
		commandService.execute(() -> {
			try {
				code.run();
			} catch (Throwable e) {
				throwableConsumer.accept(e);
			}
		});
	}

	private Consumer<Throwable> getThrowableConsumer(GenericCommandInteractionEvent event) {
		return e -> {
			final ExceptionHandler handler = context.getUncaughtExceptionHandler();
			if (handler != null) {
				handler.onException(context, event, e);

				return;
			}

			Throwable baseEx = Utils.getException(e);

			Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing an application command '" + reconstructCommand(event) + "'", baseEx);
			if (event.isAcknowledged()) {
				event.getHook().sendMessage(context.getDefaultMessages(event).getGeneralErrorMsg()).setEphemeral(true).queue();
			} else {
				event.reply(context.getDefaultMessages(event).getGeneralErrorMsg()).setEphemeral(true).queue();
			}

			context.dispatchException("Exception in application command '" + reconstructCommand(event) + "'", baseEx);
		};
	}

	private void reply(CommandInteraction event, String msg) {
		event.reply(msg)
				.setEphemeral(true)
				.queue(null,
						e -> {
							Utils.printExceptionString("Could not send reply message from application command listener", e);

							context.dispatchException("Could not send reply message from application command listener", e);
						}
				);
	}
}