package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.DefaultMessages;
import com.freya02.botcommands.api.ExceptionHandler;
import com.freya02.botcommands.api.Logging;
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
import net.dv8tion.jda.api.events.interaction.command.GenericCommandEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

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

	@Override
	public void onUserContext(@NotNull UserContextEvent event) {
		LOGGER.trace("Received user command: {}", event.getName());

		final Consumer<Throwable> throwableConsumer = getThrowableConsumer(event);
		runCommand(() -> {
			final UserCommandInfo userCommand = context.findUserCommand(event.getCommandPath());

			if (userCommand == null) {
				event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandNotFoundMsg()).queue();
				return;
			}

			if (!canRun(event, userCommand)) return;

			userCommand.execute(context, event, throwableConsumer);
		}, throwableConsumer);
	}

	@Override
	public void onMessageContext(@NotNull MessageContextEvent event) {
		LOGGER.trace("Received message command: {}", event.getName());

		final Consumer<Throwable> throwableConsumer = getThrowableConsumer(event);
		runCommand(() -> {
			final MessageCommandInfo messageCommand = context.findMessageCommand(event.getCommandPath());

			if (messageCommand == null) {
				event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandNotFoundMsg()).queue();
				return;
			}

			if (!canRun(event, messageCommand)) return;

			messageCommand.execute(context, event, throwableConsumer);
		}, throwableConsumer);
	}

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {
		LOGGER.trace("Received slash command: {}", reconstructCommand(event));

		final Consumer<Throwable> throwableConsumer = getThrowableConsumer(event);
		runCommand(() -> {
			final SlashCommandInfo slashCommand = context.findSlashCommand(CommandPath.of(event.getCommandPath()));

			if (slashCommand == null) {
				event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandNotFoundMsg()).queue();
				return;
			}

			if (!canRun(event, slashCommand)) return;

			slashCommand.execute(context, event, throwableConsumer);
		}, throwableConsumer);
	}

	@NotNull
	public static String reconstructCommand(GenericCommandEvent event) {
		if (event instanceof SlashCommandEvent slashEvent) {
			return slashEvent.getCommandString();
		} else {
			return "/" + event.getName();
		}
	}

	private boolean canRun(@NotNull GenericCommandEvent event, ApplicationCommandInfo applicationCommand) {
		final boolean isNotOwner = !context.isOwner(event.getUser().getIdLong());
		final Usability usability = Usability.of(context, event, applicationCommand, isNotOwner);

		if (usability.isUnusable()) {
			final var unusableReasons = usability.getUnusableReasons();
			if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
				reply(event, this.context.getDefaultMessages(event.getGuild()).getOwnerOnlyErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.NSFW_DISABLED)) {
				reply(event, this.context.getDefaultMessages(event.getGuild()).getNsfwDisabledErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.NSFW_ONLY)) {
				reply(event, this.context.getDefaultMessages(event.getGuild()).getNSFWOnlyErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.NSFW_DM_DENIED)) {
				reply(event, this.context.getDefaultMessages(event.getGuild()).getNSFWDMDeniedErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.USER_PERMISSIONS)) {
				reply(event, this.context.getDefaultMessages(event.getGuild()).getUserPermErrorMsg());

				return false;
			} else if (unusableReasons.contains(UnusableReason.BOT_PERMISSIONS)) {
				if (event.getGuild() == null) throw new IllegalStateException("BOT_PERMISSIONS got checked even if guild is null");

				final StringJoiner missingBuilder = new StringJoiner(", ");

				//Take needed permissions, extract bot current permissions
				final EnumSet<Permission> missingPerms = applicationCommand.getBotPermissions();
				missingPerms.removeAll(event.getGuild().getSelfMember().getPermissions(event.getTextChannel()));

				for (Permission botPermission : missingPerms) {
					missingBuilder.add(botPermission.getName());
				}

				reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getBotPermErrorMsg(), missingBuilder));

				return false;
			}
		}

		if (isNotOwner) {
			final long cooldown = applicationCommand.getCooldown(event, event::getName);
			if (cooldown > 0) {
				final DefaultMessages messages = this.context.getDefaultMessages(event.getGuild());
				if (applicationCommand.getCooldownScope() == CooldownScope.USER) {
					reply(event, String.format(messages.getUserCooldownMsg(), cooldown / 1000.0));
				} else if (applicationCommand.getCooldownScope() == CooldownScope.GUILD) {
					reply(event, String.format(messages.getGuildCooldownMsg(), cooldown / 1000.0));
				} else { //Implicit channel
					reply(event, String.format(messages.getChannelCooldownMsg(), cooldown / 1000.0));
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

	private Consumer<Throwable> getThrowableConsumer(GenericCommandEvent event) {
		return e -> {
			final ExceptionHandler handler = context.getUncaughtExceptionHandler();
			if (handler != null) {
				handler.onException(context, event, e);

				return;
			}

			Throwable baseEx = Utils.getException(e);

			Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing an application command '" + reconstructCommand(event) + "'", baseEx);
			if (event.isAcknowledged()) {
				event.getHook().sendMessage(context.getDefaultMessages(event.getGuild()).getApplicationCommandErrorMsg()).setEphemeral(true).queue();
			} else {
				event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandErrorMsg()).setEphemeral(true).queue();
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